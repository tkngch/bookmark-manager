package tkngch.bookmarkManager.js.binder

typealias EventHandler0 = () -> Unit
class Binder0 {
    private val subscribers = mutableMapOf<EventHandler0, EventHandler0>()
    fun subscribe(f: EventHandler0) = subscribers.put(f, f)
    fun trigger() = subscribers.values.forEach { it() }
    // fun unsubscribe(f: EventHandler0) = subscribers.remove(f)
}

typealias EventHandler1<T> = (T) -> Unit
class Binder1<T> {
    private val subscribers = mutableMapOf<EventHandler1<T>, EventHandler1<T>>()
    fun subscribe(f: EventHandler1<T>) = subscribers.put(f, f)
    fun trigger(x: T) = subscribers.values.forEach { it(x) }
    // fun unsubscribe(f: EventHandler1<T>) = subscribers.remove(f)
}

typealias EventHandler2<A, B> = (A, B) -> Unit
class Binder2<A, B> {
    private val subscribers = mutableMapOf<EventHandler2<A, B>, EventHandler2<A, B>>()
    fun subscribe(f: EventHandler2<A, B>) = subscribers.put(f, f)
    fun trigger(x1: A, x2: B) = subscribers.values.forEach { it(x1, x2) }
    // fun unsubscribe(f: EventHandler2<A, B>) = subscribers.remove(f)
}
