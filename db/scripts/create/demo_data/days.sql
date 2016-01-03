DECLARE
  l_first_day DATE;
  l_last_day  DATE;
  l_temp_day  DATE;
  l_day_type  VARCHAR2(16) := 'WORK_DAY';
BEGIN
  SELECT TRUNC (SYSDATE , 'YEAR') INTO l_first_day FROM DUAL;
  SELECT ADD_MONTHS(TRUNC (SYSDATE ,'YEAR'),12)-1 INTO l_last_day FROM DUAL;
  l_temp_day         := l_first_day;
  WHILE (l_temp_day != l_last_day + 1)
  LOOP
    IF (TO_CHAR(l_temp_day, 'd') in (1, 7)) THEN
      l_day_type := 'HOLIDAY';
    ELSE
      l_day_type := 'WORK_DAY';
    END IF;
    INSERT INTO DAY VALUES
      (
        gen_wa_id.nextval,
        l_temp_day,
        l_day_type
      );                                                                             
    l_temp_day := l_temp_day + 1;
  END LOOP;
END;
/