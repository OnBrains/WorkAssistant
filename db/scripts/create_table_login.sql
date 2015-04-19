CREATE TABLE LOGIN
(
  WORKER_ID NUMBER        NOT NULL,
  LOGIN     VARCHAR2(32)  NOT NULL,
  PASSWORD  VARCHAR2(512) NOT NULL
);

CREATE UNIQUE INDEX UNQ_LOGIN ON LOGIN(LOGIN);

ALTER TABLE LOGIN ADD CONSTRAINT FK_LOGIN_WORKER FOREIGN KEY (WORKER_ID) REFERENCES WORKER (ID);