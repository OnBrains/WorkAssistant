CREATE
  TABLE event
  (
    id          NUMBER PRIMARY KEY,
    title       VARCHAR2(128) NOT NULL,
    day         DATE NOT NULL,
    type_id     NUMBER NOT NULL,
    start_time  DATE,
    end_time    DATE,
    description VARCHAR2(512),
    state       VARCHAR2(16) NOT NULL
  );
  
ALTER TABLE event ADD CONSTRAINT fk_event_to_type FOREIGN KEY (type_id) REFERENCES event_type (id);
CREATE INDEX ind_event_type ON event (type_id);

COMMENT ON TABLE event IS 'События';

COMMENT ON column event.id IS 'ID';
COMMENT ON column event.title IS 'Название события';
COMMENT ON column event.day IS 'День, в который происходит событие';
COMMENT ON column event.type_id IS 'Ссылка на тип события';
COMMENT ON column event.start_time IS 'Начало события';
COMMENT ON column event.end_time IS 'Окончание события';
COMMENT ON column event.description IS 'Описание';
COMMENT ON column event.state IS 'Состояние';