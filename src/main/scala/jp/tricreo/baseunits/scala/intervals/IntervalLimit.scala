package jp.tricreo.baseunits.scala.intervals


trait LimitValue[T] extends Ordered[LimitValue[T]] {
  def toValue = this match {
    case Limit(value) => value
  }

  override def equals(obj: Any) = obj match {
    case that: LimitValue[T] => (this compare that) == 0
    case that => this match {
      case Limit(value) => value == that
      case me: Limitless[T] => false
    }
  }
}

object LimitValue {
  //  implicit def toLimitObject[T <% Ordered[T]](lv:LimitValue[T]) = lv match{
  //    case Limit(result) => result
  //  }

  //  implicit def toLimitValue[T <% Ordered[T]](limitObject:T) = limitObject match{
  //    case null => Limitless[T]
  //    case _ => Limit(limitObject)
  //  }
}

case class Limit[T <% Ordered[T]](value: T) extends LimitValue[T] {
  def compare(that: LimitValue[T]) = that match {
    case that: Limit[T] => value compare that.value
    case _ => 1
  }
}

case class Limitless[T <% Ordered[T]] extends LimitValue[T] {
  def compare(that: LimitValue[T]) = that match {
    case that: Limitless[T] => 0
    case _ => -1
  }
}

/**区間における「限界」を表すクラス。
 *
 * <p>このクラスを理解するにあたっては、「限界」と「限界値」の区別を正しく認識することが重要となる。
 * 限界とはこのクラス {@code this} で表される値であり、限界値とは、 [[#value]]で表される値である。</p>
 *
 * <p>限界が「閉じている」とは、限界値そのものを超過とみなさないことを表し、
 * 「開いている」とは、これを超過とみなすことを表す。</p>
 *
 * <p>無限限界とは、限界を制限しないことであり、 [[#value]] が {@code null} であることで
 * この状態を表現する。無限限界は常に開いていると考える。
 * 逆に、無限限界ではない限界（[[#value]] が {@code null} ではないもの）を有限限界と呼ぶ。</p>
 *
 * <p>下側限界とは、限界値以下（または未満）の値を超過とみなす限界を表し、
 * 上側限界とは、限界値以上（または超える）の値を超過とみなす限界を表す。</p>
 *
 * @tparam T 限界の型
 * @param isClosed 限界が閉じている場合 {@code true}
 * @param isLower 下側限界を表す場合は {@code true}、上側限界を表す場合は {@code false}
 * @param value 限界値 [[Limitless[T]]]の場合は、限界がないことを表す。
 */
@serializable
class IntervalLimit[T <% Ordered[T]]
(val isClosed: Boolean,
 val isLower: Boolean,
 val value: LimitValue[T])
  extends Ordered[IntervalLimit[T]] {

  private def lowerToInt(t: Int, f: Int) = if (isLower) t else f

  private def closedToInt(t: Int, f: Int) = if (isClosed) t else f

  /**
   * この限界が無限限界であるかどうかを検証する。
   *
   * @return 無限限界である場合は{@code true}、そうでない場合は{@code false}
   */
  def infinity = value match {
    case _: Limitless[T] => true
    case _ => false
  }

  /**
   * この限界が開いているかどうかを検証する。
   *
   * @return 開いている場合は{@code true}、そうでない場合は{@code false}
   */
  def isOpen = isClosed == false

  /**
   * この限界が上側限界であるかどうかを検証する。
   *
   * @return 上限値の場合は{@code true}、そうでない場合は{@code false}
   */
  def isUpper = isLower == false

  override def toString = "IntervalLimit(%s, %s, %s)".format(isClosed, isLower, value)

  override def equals(obj: Any) = obj match {
    case that: IntervalLimit[T] => compareTo(that) == 0
    case _ => false
  }

  override def hashCode = isClosed.hashCode + value.hashCode + isLower.hashCode

  /**
   * 限界同士の比較を行う。
   *
   * <p>無限限界に関して。
   * 下側の無限限界は他のいかなる限界よりも「小さい」と判断し、
   * 上側の無限限界は他のいかなる限界よりも「大きい」と判断する。
   * 同じ側の限界同士の比較では「同一」と判断する。</p>
   *
   * <p>有限限界同士の比較に関して。
   * この場合は、それぞれの限界の開閉や上下にかかわらず、限界値が小さい方を「小さい」と判断する。
   * 限界値が同一である場合、下側限界同士の比較である場合は閉じている方を「小さい」と判断し、
   * 上側限界同士の比較である場合は閉じている方を「大きい」と判断する。
   * 限界値が同一で、上側限界と下側限界の比較の場合は、開閉にかかわらず下側を「小さい」と判断する。</p>
   *
   * @param other 比較対象
   * @return 同値であった場合は {@code 0}、このオブジェクトが比較対象よりも小さい場合は負数、大きい場合は正数
   */
  def compare(obj: IntervalLimit[T]): Int = {
    if (value.isInstanceOf[Limitless[T]] && obj.value.isInstanceOf[Limitless[T]]) {
      if (isLower == obj.isLower) {
        return 0
      }
      return lowerToInt(-1, 1)
    }
    if (value.isInstanceOf[Limitless[T]]) {
      return lowerToInt(-1, 1)
    }
    if (obj.value.isInstanceOf[Limitless[T]]) {
      return obj.lowerToInt(1, -1)
    }
    if (value == obj.value) {
      if (isLower && obj.isLower) {
        if (isClosed ^ obj.isClosed) {
          return closedToInt(-1, 1)
        }
        return 0
      }
      if (isLower == false && obj.isLower == false) {
        if (isClosed ^ obj.isClosed) {
          return closedToInt(1, -1)
        }
        return 0
      }
      return lowerToInt(-1, 1)
    }
    return value compare obj.value
  }
}

/**
 * 区間における「限界」を表すコンパニオンオブジェクト。
 *
 */
object IntervalLimit {

  /**
   * インスタンスを生成する。
   *
   * <p>無限限界（{@code value}ば{@code null}だった場合は、{@code isClosed}の指定にかかわらず
   * 常に閉じた限界のインスタンスを生成する。</p>
   *
   * @param isClosed 閉じた限界を生成する場合は {@code true}を指定する
   * @param isLower 下側限界を生成する場合は {@code true}、上側限界を生成する場合は {@code false}を指定する
   * @param value 限界値. {@code null}の場合は、限界がないことを表す
   */
  def apply[T <% Ordered[T]](isClosed: Boolean, isLower: Boolean, value: LimitValue[T]) =
    new IntervalLimit[T](if (value.isInstanceOf[Limitless[T]]) false else isClosed, isLower, value)


  /**
   * 抽出子メソッド。
   *
   * @param intervalLimit [[IntervalLimit]]
   * @return Option[(Boolean, Boolean, T)]
   */
  def unapply[T <% Ordered[T]](intervalLimit: IntervalLimit[T]): Option[(Boolean, Boolean, LimitValue[T])] =
    Some(intervalLimit.isClosed, intervalLimit.isLower, intervalLimit.value)

  /**
   * 下側限界インスタンスを生成する。
   *
   * @param <T> 限界値の型
   * @param isClosed 閉じた限界を生成する場合は {@code true}を指定する
   * @param value 限界値. {@code null}の場合は、限界がないことを表す
   * @return 下側限界インスタンス
   */
  def lower[T <% Ordered[T]](isClosed: Boolean, value: LimitValue[T]) = apply(isClosed, true, value)

  /**
   * 上側限界インスタンスを生成する。
   *
   * @param <T> 限界値の型
   * @param isClosed 閉じた限界を生成する場合は {@code true}を指定する
   * @param value 限界値. {@code null}の場合は、限界がないことを表す
   * @return 上側限界インスタンス
   */
  def upper[T <% Ordered[T]](isClosed: Boolean, value: LimitValue[T]) = apply(isClosed, false, value)

}
