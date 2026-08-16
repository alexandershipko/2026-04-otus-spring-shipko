Требования к реализации:

0. Добавить в приложение новую сущность - пользователь. Не обязательно реализовывать методы по созданию пользователей - допустимо добавить пользователей только через БД-скрипты.
1. В существующее CRUD-приложение добавить механизм Form-based аутентификации.
2. UserDetailsService реализовать самостоятельно.
3. Авторизация на всех страницах - для всех аутентифицированных. Форма логина - доступна для всех.
4. Написать тесты контроллеров, которые проверяют, что все необходимые ресурсы действительно защищены.

user / userpass
admin / adminpass
---------------------------

Локализация в Spring Boot

0. Spring Boot автоконфигурирует LocaleResolver и MessageSource.
1. По умолчанию MessageSource берёт бандлы в src/main/resources :
   src/main/resources/messages.properties – default locale (required!)
   src/main/resources/messages_en_US.properties – конкретные локали
2. Все файлы читаются в UTF-8
3. Вы можете поменять местонахождение бандлов в application.yml:
   spring.messages.basename: path/to/i18n/messages