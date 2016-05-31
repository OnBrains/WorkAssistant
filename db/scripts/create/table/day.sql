CREATE
  TABLE day
  (
    id   NUMBER PRIMARY KEY,
    date DATE NOT NULL,
    type VARCHAR2(16) NOT NULL
  );
  
CREATE UNIQUE INDEX unq_day ON day (date);

COMMENT ON TABLE day IS 'Перечень дней года';

COMMENT ON column day.id IS 'ID';
COMMENT ON column day.date IS 'Дата';
COMMENT ON column day.type IS 'Тип для Рабочий/Выходной/Сокращенный';