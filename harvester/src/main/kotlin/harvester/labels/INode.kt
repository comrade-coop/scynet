package harvester.labels

class INode<E> {
    internal var item: E? = null
    internal var next: INode<E>? = null
    internal var prev: INode<E>? = null
}