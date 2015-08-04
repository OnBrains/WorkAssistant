declare
  l_day date;
  l_day_index number := 0;
begin
  select add_months(sysdate,-7) + l_day_index into l_day from dual;
  while (to_char(l_day) != '31.12.15')
  loop 
    select add_months(sysdate,-7) + l_day_index into l_day from dual;
    insert into day values (gen_wa_id.nextval, l_day, 'WORK_DAY');
    l_day_index := l_day_index + 1;
  end loop;
end;
/

declare
  l_suterday_index number := 5;
begin
  for x in (select * from day order by id)
  loop
    if (x.id = l_suterday_index) then
      update day d
        set day_type = 'HOLIDAY'
      where x.id = d.id or x.id = d.id+1;
      l_suterday_index := l_suterday_index + 7;
    end if;
  end loop;
end;
/