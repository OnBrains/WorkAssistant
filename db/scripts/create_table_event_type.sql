CREATE TABLE EVENT_TYPE
(
  ID         NUMBER PRIMARY KEY,
  NAME       VARCHAR2(16)           NOT NULL,
  IS_WORKING NUMBER(1, 0) DEFAULT 1 NOT NULL,
  TIME       NUMBER DEFAULT 0       NOT NULL,
  IS_SYS     NUMBER DEFAULT 0       NOT NULL
);

COMMENT ON TABLE EVENT_TYPE IS 'Типы событий рабочего для: митинги, планирования, обеды и тд.';
COMMENT ON COLUMN EVENT_TYPE.ID IS 'PK';
COMMENT ON COLUMN EVENT_TYPE.NAME IS 'Наименование события';
COMMENT ON COLUMN EVENT_TYPE.IS_WORKING IS 'Считается ли событие рабочим';
COMMENT ON COLUMN EVENT_TYPE.TIME IS 'Время которое не считается недоработкой, для не рабочего события';
COMMENT ON COLUMN EVENT_TYPE.IS_SYS IS 'Является ли запись системной';

CREATE UNIQUE INDEX UNQ_EVENT_TYPE ON EVENT_TYPE (NAME);