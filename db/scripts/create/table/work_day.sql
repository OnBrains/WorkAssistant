CREATE
  TABLE work_day
  (
    id          NUMBER PRIMARY KEY,
    worker_id   NUMBER NOT NULL,
    day_id      NUMBER NOT NULL,
    coming_time DATE,
    out_time    DATE,
    state       VARCHAR2(16) NOT NULL
  );
  
ALTER TABLE work_day ADD CONSTRAINT fk_wd_to_worker FOREIGN KEY (worker_id) REFERENCES worker (id);
CREATE INDEX ind_wd_worker ON work_day (worker_id);

ALTER TABLE work_day ADD CONSTRAINT fk_wd_to_day FOREIGN KEY (day_id) REFERENCES day (id);
CREATE INDEX ind_wd_day ON work_day (day_id);

CREATE UNIQUE INDEX unq_work_day on work_day (worker_id, day_id);

COMMENT ON TABLE work_day IS 'Рабочии дни для конкретного работника';

COMMENT ON column work_day.id IS 'ID';
COMMENT ON column work_day.worker_id IS 'Ссылка на работника';
COMMENT ON column work_day.day_id IS 'Ссылка на день';
COMMENT ON column work_day.coming_time IS 'Время начала рабочего дня';
COMMENT ON column work_day.out_time IS 'Время окончания рабочего дня';
COMMENT ON column work_day.state IS 'Состояние рабочего дня';