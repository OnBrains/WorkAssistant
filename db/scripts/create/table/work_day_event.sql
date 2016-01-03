CREATE
  TABLE work_day_event
  (
    work_day_id NUMBER NOT NULL,
    event_id  NUMBER NOT NULL
  );

ALTER TABLE work_day_event ADD CONSTRAINT fk_wde_to_work_day FOREIGN KEY (work_day_id) REFERENCES work_day (id);
CREATE INDEX ind_wde_work_day ON work_day_event (work_day_id);

ALTER TABLE work_day_event ADD CONSTRAINT fk_wde_to_event FOREIGN KEY (event_id) REFERENCES event (id);
CREATE INDEX ind_wde_event ON work_day_event (event_id);



COMMENT ON TABLE work_day_event IS '�������� ����� �������� ����� � ���������';

COMMENT ON column work_day_event.work_day_id IS '������ �� ������� ����';
COMMENT ON column work_day_event.event_id IS '������ �� �������';