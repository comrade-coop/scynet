using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A receiver of messages comming from a registry
    /// </summary>
    public interface IRegistryListener<T> : Orleans.IGrainObserver
    {
        /// <summary>
        /// Receive an item.
        /// </summary>
        /// <param name="queryIdentifier">A string to help identify which query the item comes from.</param>
        /// <param name="item">The new item.</param>
        void NewItem(String queryIdentifier, T item);
    }
}
