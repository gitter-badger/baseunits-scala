package jp.tricreo.baseunits.scala.time

import jp.tricreo.baseunits.scala.intervals.Limit

/**
 * Created by IntelliJ IDEA.
 * User: junichi
 * Date: 11/04/19
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */

abstract class MonthlyDateSpecification extends DateSpecification {

  override def firstOccurrenceIn(interval: CalendarInterval) = {
    val month = interval.start.toValue.asCalendarMonth

    val firstTry = ofYearMonth(month)
    if (interval.includes(Limit(firstTry.get))) {
      firstTry
    } else {
      val secondTry = ofYearMonth(month.nextMonth)
      if (interval.includes(Limit(secondTry.get))) {
        secondTry
      } else None
    }
  }

  override def iterateOver(interval: CalendarInterval) = {
    new Iterator[CalendarDate] {

      var _next = firstOccurrenceIn(interval)

      var _month = next.asCalendarMonth


      override def hasNext = next != None

      override def next = {
        if (hasNext == false) {
          throw new NoSuchElementException
        }
        val current = _next
        _month = _month.nextMonth
        _next = MonthlyDateSpecification.this.ofYearMonth(_month)
        if (interval.includes(Limit(_next.get)) == false) {
          _next = None
        }
        current.get
      }
    };
  }

  /**
   * 指定した年月においてこの日付仕様を満たす年月日を返す。
   *
   * @param month 年月
   * @return {@link CalendarDate}
   * @throws IllegalArgumentException 引数に{@code null}を与えた場合
   */
  def ofYearMonth(month: CalendarMonth): Option[CalendarDate]
}