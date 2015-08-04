package org.onbrains.entity.event;

import org.onbrains.entity.SuperClass;

import javax.persistence.*;

/**
 * @author Naumov Oleg on 18.04.2015 13:41.
 * <p/>
 * Типы событий, которые могут происходить в течении рабочего дня.
 * Типы событий можно разделить на следующие категории:
 * <ul>
 *     <li>События, время которых идет в счет отработанного</li>
 *     <li>События, время которых не идет в счет отработанного</li>
 *     <li>События, время которых никак не влияет на отработанное время</li>
 * </ul>
 * Для определения влияет ли событие на отработанное время используется {@linkplain #isWorking флаг},
 * для событий, время которых не идет в счет отработанного может допускаться какое то количество времени,
 * которое {@linkplain #notWorkingTime не надо отрабатыват}
 */
@Entity
@Table(name = "EVENT_TYPE", uniqueConstraints = {@UniqueConstraint(columnNames = {"TITLE"})})
public class EventType extends SuperClass {

    public static final Long WORK_EVENT_TYPE_ID = 2275L;

    @Column(name = "TITLE", nullable = false, length = 64)
    private String title;

    @Column(name = "DESCRIPTION", nullable = true, length = 512)
    private String description;

    @Column(name = "IS_WORKING", nullable = false)
    private boolean isWorking;

    @Column(name = "NOT_WORKING_TIME", nullable = true)
    private Long notWorkingTime = 0L;

    protected EventType() {
        EventType eventType;
    }

    /**
     * @return Наименование типа события.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Подробное описание типа собыия.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Определяет влияет ли событие на отработанное время.
     * <p/>
     * Пример событий, время которых идет в зачет отработанного:
     * <ul>
     *     <li>Сам рабочий процесс;</li>
     *     <li>Командировка местная/дальнего плавания.</li>
     * </ul>
     * Пример событий, которые не влияют на рабочее вермя:
     * <ul>
     *     <li>Совещания;</li>
     *     <li>Миттинги.</li>
     * </ul>
     * Пример событий, время которых не идет в зачет отработанного:
     * <ul>
     *     <li>Отгул.</li>
     * </ul>
     * @return <strong>true</strong> - если влияет.
     */
    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    /**
     * Для событий, время которых не идет в зачет отработанного возможно указать, какое количество времени
     * разрещается не отрабатывать.
     * <p/>
     * Примеры событий, для которых допускается не отрабатывать определенное количество времени:
     * <table border="1">
     *     <tr>
     *         <td>Тип события</td>
     *         <td>Можно не отрабатывать</td>
     *     </tr>
     *     <tr>
     *         <td>Обед вне офиса</td>
     *         <td> - 30 мин.</td>
     *     </tr>
     *     <tr>
     *         <td>Отпуск</td>
     *         <td> - 8 ч. 30 мин. (весь рабочий день)</td>
     *     </tr>
     *     <tr>
     *         <td>Больничный</td>
     *         <td> - 8 ч. 30 мин. (весь рабочий день)</td>
     *     </tr>
     * </table>
     *
     * @return количество времени, которое будет считаться за отработанное(в мс).
     */
    public Long getNotWorkingTime() {
        return notWorkingTime;
    }

    public void setNotWorkingTime(Long time) {
        this.notWorkingTime = time;
    }

}