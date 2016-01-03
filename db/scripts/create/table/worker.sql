CREATE
  TABLE worker
  (
    id           NUMBER PRIMARY KEY,
    family       VARCHAR2(64) NOT NULL,
    first_name   VARCHAR2(32) NOT NULL,
    surname      VARCHAR2(32) NOT NULL,
    birthday     DATE NOT NULL,
    sex          VARCHAR2(16) NOT NULL,
    mobile_phone VARCHAR2(32) NOT NULL,
    email        VARCHAR2(64) NOT NULL
  );
  
COMMENT ON TABLE worker IS 'Работкники/Пользователи';
  
COMMENT ON column worker.id IS 'ID пользователя';
COMMENT ON column worker.family IS 'Фамилия';
COMMENT ON column worker.first_name IS 'Имя';
COMMENT ON column worker.surname IS 'Отчество';
COMMENT ON column worker.birthday IS 'День рождения';
COMMENT ON column worker.sex IS 'Пол';
COMMENT ON column worker.mobile_phone IS 'Номер мобильного телефона';
COMMENT ON column worker.email IS 'Адрес электронной почты';