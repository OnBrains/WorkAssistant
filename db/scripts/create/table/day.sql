CREATE
  TABLE day
  (
    id   NUMBER PRIMARY KEY,
    date DATE NOT NULL,
    type VARCHAR2(16) NOT NULL
  );
  
CREATE UNIQUE INDEX unq_day ON day (date);

COMMENT ON TABLE day IS '�������� ���� ����';

COMMENT ON column day.id IS 'ID';
COMMENT ON column day.date IS '����';
COMMENT ON column day.type IS '��� ��� �������/��������/�����������';