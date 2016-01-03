CREATE
  TABLE login
  (
    id        NUMBER PRIMARY KEY,
    worker_id NUMBER NOT NULL,
    login     VARCHAR2(32) NOT NULL,
    password  VARCHAR2(512) NOT NULL
  );
  
ALTER TABLE login ADD CONSTRAINT fk_login_to_worker FOREIGN KEY (worker_id) REFERENCES worker (id);
CREATE INDEX ind_login_worker ON login (worker_id);

CREATE UNIQUE INDEX unq_login on login (login);
  
COMMENT ON TABLE login IS 'Логин и пароль для работника/пользователя';

COMMENT ON column login.id IS 'ID';
COMMENT ON column login.worker_id IS 'Ссылка на работника';
COMMENT ON column login.login IS 'Логин для авторизации';
COMMENT ON column login.password IS 'Пароль для авторизации';