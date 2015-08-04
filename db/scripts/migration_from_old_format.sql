--delete from work_assistent.work_day;

insert into work_assistent.work_day
select  
    work_assistent.gen_wa_id.nextval,
--    d.day,
    to_char(wd.coming_time, 'dd.mm.yyyy hh24:mm:ss'),
    to_char(wd.out_time, 'dd.mm.yyyy hh24:mm:ss'),
    wd.state,
    d.id,
    1
  from work_day wd,
    work_assistent.day d
  where to_char(wd.day, 'dd.mm.yyyy') = to_char(d.day, 'dd.mm.yyyy') and wd.worker_id = 1; 
  
  
insert into event
select   gen_wa_id.nextval,
    d.day,
    '',
    wd.out_time,
    wd.coming_time,
    'Работа за '||to_char(d.day, 'dd.mm.yy'),
    2275
  from work_day wd,
    day d
  where wd.day_id = d.id;  