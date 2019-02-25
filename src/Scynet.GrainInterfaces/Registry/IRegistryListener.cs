using System;

namespace Scynet.GrainInterfaces.Registry
{
    /// <summary>
    /// A receiver of messages comming from a registry
    /// </summary>
    public interface IRegistryListener<K, T> : Orleans.IGrainObserver
    {
        /// <summary>
        /// Receive an item.
        /// </summary>
        /// <param name="queryIdentifier">A string to help identify which query the item comes from.</param>
        /// <param name="item">The new item.</param>
        void NewItem(String queryIdentifier, K key, T item);
    }
}
