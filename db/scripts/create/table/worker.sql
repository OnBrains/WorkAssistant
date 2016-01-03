CREATE
  TABLE worker
  (
    id           NUMBER PRIMARY KEY,
    family       VARCHAR2(64) NOT NULL,
    first_name   VARCHAR2(32) NOT NULL,
    surname      VARCHAR2(32) NOT NULL,
    birthday     DATE NOT NULL,
    sex          VARCHAR2(16) NOT NULL,
    mobile_phone VARCHAR2(32) NOT NULL,
    email        VARCHAR2(64) NOT NULL
  );
  
COMMENT ON TABLE worker IS '����������/������������';
  
COMMENT ON column worker.id IS 'ID ������������';
COMMENT ON column worker.family IS '�������';
COMMENT ON column worker.first_name IS '���';
COMMENT ON column worker.surname IS '��������';
COMMENT ON column worker.birthday IS '���� ��������';
COMMENT ON column worker.sex IS '���';
COMMENT ON column worker.mobile_phone IS '����� ���������� ��������';
COMMENT ON column worker.email IS '����� ����������� �����';