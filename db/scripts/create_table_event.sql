CREATE TABLE EVENT
(
  ID          NUMBER PRIMARY KEY,
  TYPE_ID     NUMBER        NOT NULL,
  TITLE       VARCHAR2(128) NOT NULL,
  DESCRIPTION VARCHAR2(512),
  START_TIME  TIMESTAMP     NOT NULL,
  END_TIME    TIMESTAMP     NOT NULL
);

COMMENT ON TABLE EVENT IS 'События рабочего для';
COMMENT ON COLUMN EVENT.ID IS 'PK';
COMMENT ON COLUMN EVENT.TYPE_ID IS 'Ссылка на тип события';
COMMENT ON COLUMN EVENT.TITLE IS 'Название события';
COMMENT ON COLUMN EVENT.DESCRIPTION IS 'Описание события';
COMMENT ON COLUMN EVENT.START_TIME IS 'Время начала';
COMMENT ON COLUMN EVENT.END_TIME IS 'Время окончания';