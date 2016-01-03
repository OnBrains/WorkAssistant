CREATE
  TABLE worker_event
  (
    worker_id NUMBER NOT NULL,
    event_id  NUMBER NOT NULL
  );

ALTER TABLE worker_event ADD CONSTRAINT fk_we_to_worker FOREIGN KEY (worker_id) REFERENCES worker (id);
CREATE INDEX ind_we_worker ON worker_event (worker_id);

ALTER TABLE worker_event ADD CONSTRAINT fk_we_to_event FOREIGN KEY (event_id) REFERENCES event (id);
CREATE INDEX ind_we_event ON worker_event (event_id);



COMMENT ON TABLE worker_event IS 'Развязка между работниками и событиями';

COMMENT ON column worker_event.worker_id IS 'Ссылка на работника';
COMMENT ON column worker_event.event_id IS 'Ссылка на событие';