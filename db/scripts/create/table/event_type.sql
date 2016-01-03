CREATE
  TABLE event_type
  (
    id               NUMBER PRIMARY KEY,
    title            VARCHAR2(64) NOT NULL,
    category         VARCHAR2(32) NOT NULL,
    not_working_time NUMBER DEFAULT 0 NOT NULL,
    active           NUMBER(1) NOT NULL,
    description      VARCHAR2(512)
  );
  
CREATE UNIQUE INDEX unq_event_type on event_type (title);

COMMENT ON TABLE event_type IS '���������� ����� �������';

COMMENT ON column event_type.id IS 'ID';
COMMENT ON column event_type.title IS '������������';
COMMENT ON column event_type.category IS '��������� �������';
COMMENT ON column event_type.not_working_time IS '���-�� �������, ������� ��������� �� �������';
COMMENT ON column event_type.active IS '������ ����������';
COMMENT ON column event_type.description IS '�������� �������';