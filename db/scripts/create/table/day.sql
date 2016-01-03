CREATE
  TABLE DAY
  (
    id   NUMBER PRIMARY KEY,
    DAY  DATE NOT NULL,
    type VARCHAR2(16) NOT NULL
  );
  
CREATE UNIQUE INDEX unq_day ON DAY (DAY);

COMMENT ON TABLE day IS '�������� ���� ����';

COMMENT ON column day.id IS 'ID';
COMMENT ON column day.day IS '����';
COMMENT ON column day.type IS '��� ��� �������/��������/�����������';