#!/usr/bin/env bash
# Проверка сценариев из таблицы 8 курсовой на запущенном приложении.
# Запускается в GitHub Actions (файл .github/workflows/build.yml).

BASE=http://localhost:8080
USER1=$(mktemp)   # cookie первого пользователя
USER2=$(mktemp)   # cookie второго пользователя
BODY=$(mktemp)    # html последнего ответа

# Ждем, пока приложение запустится (до 2 минут)
for i in $(seq 1 60); do
    curl -s -o /dev/null "$BASE/login" && break
    sleep 2
done

passed=0
ok()   { echo "OK      $1"; passed=$((passed + 1)); }
fail() { echo "ОШИБКА  $1"; echo "--- ответ сервера:"; cat "$BODY"; exit 1; }

# Открыть страницу и вывести ее html
page() { curl -s -b "$1" -c "$1" "$BASE$2"; }

# Открыть страницу без перехода по redirect, вывести "код адрес_перехода"
visit() { curl -s -o "$BODY" -w '%{http_code} %{redirect_url}' -b "$1" -c "$1" "$BASE$2"; }

# Отправить форму, вывести "код адрес_перехода" (html ответа попадает в $BODY)
send() {
    local jar=$1 path=$2
    shift 2
    curl -s -o "$BODY" -w '%{http_code} %{redirect_url}' -b "$jar" -c "$jar" "$@" "$BASE$path"
}

# CSRF-токен из формы на странице (без него Spring Security не примет форму)
token() { page "$1" "$2" | grep -o '<input[^>]*name="_csrf"[^>]*>' | head -1 | sed -E 's/.*value="([^"]*)".*/\1/'; }

register() {  # register <cookie> <логин> <пароль>
    send "$1" /register --data-urlencode "login=$2" --data-urlencode "password=$3" \
        --data-urlencode "_csrf=$(token "$1" /register)"
}

login() {  # login <cookie> <логин> <пароль>
    send "$1" /login --data-urlencode "username=$2" --data-urlencode "password=$3" \
        --data-urlencode "_csrf=$(token "$1" /login)"
}

save() {  # save <cookie> <адрес формы> <название> <инв. номер> <кабинет> <тип>
    send "$1" "$2" --data-urlencode "name=$3" --data-urlencode "inventoryNumber=$4" \
        --data-urlencode "room=$5" --data-urlencode "equipmentType=$6" \
        --data-urlencode "_csrf=$(token "$1" "$2")"
}

# id первой записи в списке (из ссылки «Изменить»)
first_id() { page "$1" "$2" | grep -o '/equipment/[0-9]*/edit' | head -1 | grep -o '[0-9]\+'; }

echo "Проверка сценариев:"

r=$(register "$USER1" ivanov 1234)
if [[ $r == 302*"/login?registered" ]] && page "$USER1" "/login?registered" | grep -q "Регистрация прошла успешно"
then ok "1. Регистрация с новым логином"; else fail "1. Регистрация с новым логином ($r)"; fi

r=$(register "$USER1" ivanov 5678)
if [[ $r == 200* ]] && grep -q "Такой логин уже занят" "$BODY"
then ok "2. Регистрация с занятым логином"; else fail "2. Регистрация с занятым логином ($r)"; fi

r=$(register "$USER1" petrov 12)
if [[ $r == 200* ]] && grep -q "Пароль должен быть не короче 4 символов" "$BODY"
then ok "3. Регистрация с коротким паролем"; else fail "3. Регистрация с коротким паролем ($r)"; fi

r=$(login "$USER1" ivanov 0000)
if [[ $r == 302*"/login?error" ]] && page "$USER1" "/login?error" | grep -q "Неверный логин или пароль"
then ok "4. Вход с неверным паролем"; else fail "4. Вход с неверным паролем ($r)"; fi

r=$(curl -s -o "$BODY" -w '%{http_code} %{redirect_url}' "$BASE/equipment")
if [[ $r == 302*"/login" ]]
then ok "5. Открытие /equipment без входа"; else fail "5. Открытие /equipment без входа ($r)"; fi

r=$(login "$USER1" ivanov 1234)
[[ $r == 302*"/equipment" ]] || fail "Вход с верным паролем ($r)"

r=$(save "$USER1" /equipment/new "" "ИНВ-000999" 101 PRINTER)
list=$(page "$USER1" /equipment)
if [[ $r == 200* ]] && grep -q "Укажите название" "$BODY" && grep -q "Записей нет" <<< "$list"
then ok "6. Добавление с пустым названием"; else fail "6. Добавление с пустым названием ($r)"; fi

r1=$(save "$USER1" /equipment/new "МФУ HP LaserJet Pro M428" "ИНВ-000131" 101 PRINTER)
r2=$(save "$USER1" /equipment/new "Монитор Samsung 24" "ИНВ-000245" 101 MONITOR)
list=$(page "$USER1" /equipment)
if [[ $r1 == 302*"/equipment" && $r2 == 302*"/equipment" ]] \
    && grep -q "МФУ HP LaserJet Pro M428" <<< "$list" && grep -q "Монитор Samsung 24" <<< "$list"
then ok "7. Добавление корректной записи"; else fail "7. Добавление корректной записи ($r1, $r2)"; fi

printer=$(first_id "$USER1" "/equipment?type=PRINTER")
monitor=$(first_id "$USER1" "/equipment?type=MONITOR")

r=$(save "$USER1" "/equipment/$printer/edit" "МФУ HP LaserJet Pro M428" "ИНВ-000131" 205 PRINTER)
if [[ $r == 302*"/equipment" ]] && page "$USER1" /equipment | grep -q "<td>205</td>"
then ok "8. Редактирование записи"; else fail "8. Редактирование записи ($r)"; fi

list=$(page "$USER1" "/equipment?type=PRINTER")
if grep -q "МФУ HP LaserJet Pro M428" <<< "$list" && ! grep -q "Монитор Samsung 24" <<< "$list"
then ok "9. Фильтр по типу «Принтер»"; else fail "9. Фильтр по типу «Принтер»"; fi

r=$(send "$USER1" "/equipment/$monitor/delete" --data-urlencode "_csrf=$(token "$USER1" /equipment)")
list=$(page "$USER1" /equipment)
if [[ $r == 302*"/equipment" ]] && grep -q "МФУ HP LaserJet Pro M428" <<< "$list" && ! grep -q "Монитор Samsung 24" <<< "$list"
then ok "10. Удаление записи"; else fail "10. Удаление записи ($r)"; fi

register "$USER2" petrov 4321 > /dev/null
r=$(login "$USER2" petrov 4321)
list=$(page "$USER2" /equipment)
if [[ $r == 302*"/equipment" ]] && grep -q "Записей нет" <<< "$list" && ! grep -q "МФУ HP LaserJet Pro M428" <<< "$list"
then ok "11. Вход вторым пользователем"; else fail "11. Вход вторым пользователем ($r)"; fi

r=$(visit "$USER2" "/equipment/$printer/edit")
if [[ $r == 302*"/equipment" ]]
then ok "12. Открытие чужой записи по адресу"; else fail "12. Открытие чужой записи по адресу ($r)"; fi

echo "Пройдено сценариев: $passed из 12"
