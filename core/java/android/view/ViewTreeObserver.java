/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.graphics.Rect;
import android.graphics.Region;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ViewTreeObserver不能够被应用程序实例化，因为它是由视图提供，参照getViewTreeObserver()以查看更多信息。
 * 视图树检查器用于向视图树中注册在视图树发生全局变更时可以发出通知的监听器.
 * 这些全局事件包括但不限于整个树的布局事件、开始绘制事件、触控模式变更事件.
 *
 * 应用程序不能实例化ViewTreeObserver，其实例由试图层次提供.
 * 更多信息请参考{@link android.view.View#getViewTreeObserver()}函数.
 */
public final class ViewTreeObserver {
    // Recursive listeners use CopyOnWriteArrayList
    private CopyOnWriteArrayList<OnGlobalFocusChangeListener> mOnGlobalFocusListeners;
    private CopyOnWriteArrayList<OnTouchModeChangeListener> mOnTouchModeChangeListeners;

    // Non-recursive listeners use CopyOnWriteArray
    // Any listener invoked from ViewRootImpl.performTraversals() should not be recursive
    private CopyOnWriteArray<OnGlobalLayoutListener> mOnGlobalLayoutListeners;
    private CopyOnWriteArray<OnComputeInternalInsetsListener> mOnComputeInternalInsetsListeners;
    private CopyOnWriteArray<OnScrollChangedListener> mOnScrollChangedListeners;
    private CopyOnWriteArray<OnPreDrawListener> mOnPreDrawListeners;

    // These listeners cannot be mutated during dispatch
    private ArrayList<OnDrawListener> mOnDrawListeners;

    private boolean mAlive = true;

    /**
     * 为视图树的焦点状态发生变化时执行的回调函数定义的接口.
     */
    public interface OnGlobalFocusChangeListener {
        /**
         * 视图树的焦点状态发生变化时执行的回调函数.当视图树由触控模式变为非触控模式时，
         * oldFocus为空.当视图树由非触控模式变为触控模式时，newFocus为空.
         * 在非触控模式焦点变更时（不发生触控模式变更），oldFocus和newFocus都可以为空.
         *
         * @param oldFocus 若不为空，则为之前具有焦点视图.
         * @param newFocus 若不为空，则为得到焦点的视图.
         */
        public void onGlobalFocusChanged(View oldFocus, View newFocus);
    }

    /**
     * 为视图树的可视性或全局布局状态发生变化时执行的回调函数定义的接口.
     */
    public interface OnGlobalLayoutListener {
        /**
         * 视图树的可视性或全局布局状态发生变化时执行的回调函数.
         */
        public void onGlobalLayout();
    }

    /**
     * 为即将绘制视图树时执行的回调函数定义的接口.
     */
    public interface OnPreDrawListener {
        /**
         * 即将绘制视图树时执行的回调函数.这时所有的视图都测量完成并确定了框架.
         * 客户端可以使用该方法来调整滚动边框，甚至可以在绘制之前请求新的布局.
         *
         * @return 继续当前绘制处理返回真；终止返回假.
         *
         * @see android.view.View#onMeasure
         * @see android.view.View#onLayout
         * @see android.view.View#onDraw
         */
        public boolean onPreDraw();
    }

    /**
     * Interface definition for a callback to be invoked when the view tree is about to be drawn.
     */
    public interface OnDrawListener {
        /**
         * <p>Callback method to be invoked when the view tree is about to be drawn. At this point,
         * views cannot be modified in any way.</p>
         * 
         * <p>Unlike with {@link OnPreDrawListener}, this method cannot be used to cancel the
         * current drawing pass.</p>
         * 
         * <p>An {@link OnDrawListener} listener <strong>cannot be added or removed</strong>
         * from this method.</p>
         *
         * @see android.view.View#onMeasure
         * @see android.view.View#onLayout
         * @see android.view.View#onDraw
         */
        public void onDraw();
    }

    /**
     * 为触控模式变化时执行的回调函数定义的接口.
     */
    public interface OnTouchModeChangeListener {
        /**
         * 触摸模式发生改变时调用的回调函数.
         *
         * @param isInTouchMode 如果视图结构当前处于触摸模式，参数为真；否则为假.
         */
        public void onTouchModeChanged(boolean isInTouchMode);
    }

    /**
     * 为视图树发生滚动时执行的回调函数定义的接口.
     */
    public interface OnScrollChangedListener {
        /**
         * 视图树发生滚动时执行的回调函数.
         */
        public void onScrollChanged();
    }

    /**
     * Parameters used with OnComputeInternalInsetsListener.
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public final static class InternalInsetsInfo {
        /**
         * Offsets from the frame of the window at which the content of
         * windows behind it should be placed.
         */
        public final Rect contentInsets = new Rect();

        /**
         * Offsets from the frame of the window at which windows behind it
         * are visible.
         */
        public final Rect visibleInsets = new Rect();

        /**
         * Touchable region defined relative to the origin of the frame of the window.
         * Only used when {@link #setTouchableInsets(int)} is called with
         * the option {@link #TOUCHABLE_INSETS_REGION}.
         */
        public final Region touchableRegion = new Region();

        /**
         * Option for {@link #setTouchableInsets(int)}: the entire window frame
         * can be touched.
         */
        public static final int TOUCHABLE_INSETS_FRAME = 0;

        /**
         * Option for {@link #setTouchableInsets(int)}: the area inside of
         * the content insets can be touched.
         */
        public static final int TOUCHABLE_INSETS_CONTENT = 1;

        /**
         * Option for {@link #setTouchableInsets(int)}: the area inside of
         * the visible insets can be touched.
         */
        public static final int TOUCHABLE_INSETS_VISIBLE = 2;

        /**
         * Option for {@link #setTouchableInsets(int)}: the area inside of
         * the provided touchable region in {@link #touchableRegion} can be touched.
         */
        public static final int TOUCHABLE_INSETS_REGION = 3;

        /**
         * Set which parts of the window can be touched: either
         * {@link #TOUCHABLE_INSETS_FRAME}, {@link #TOUCHABLE_INSETS_CONTENT},
         * {@link #TOUCHABLE_INSETS_VISIBLE}, or {@link #TOUCHABLE_INSETS_REGION}.
         */
        public void setTouchableInsets(int val) {
            mTouchableInsets = val;
        }

        int mTouchableInsets;

        void reset() {
            contentInsets.setEmpty();
            visibleInsets.setEmpty();
            touchableRegion.setEmpty();
            mTouchableInsets = TOUCHABLE_INSETS_FRAME;
        }

        @Override
        public int hashCode() {
            int result = contentInsets != null ? contentInsets.hashCode() : 0;
            result = 31 * result + (visibleInsets != null ? visibleInsets.hashCode() : 0);
            result = 31 * result + (touchableRegion != null ? touchableRegion.hashCode() : 0);
            result = 31 * result + mTouchableInsets;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InternalInsetsInfo other = (InternalInsetsInfo)o;
            return mTouchableInsets == other.mTouchableInsets &&
                    contentInsets.equals(other.contentInsets) &&
                    visibleInsets.equals(other.visibleInsets) &&
                    touchableRegion.equals(other.touchableRegion);
        }

        void set(InternalInsetsInfo other) {
            contentInsets.set(other.contentInsets);
            visibleInsets.set(other.visibleInsets);
            touchableRegion.set(other.touchableRegion);
            mTouchableInsets = other.mTouchableInsets;
        }
    }

    /**
     * Interface definition for a callback to be invoked when layout has
     * completed and the client can compute its interior insets.
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public interface OnComputeInternalInsetsListener {
        /**
         * Callback method to be invoked when layout has completed and the
         * client can compute its interior insets.
         *
         * @param inoutInfo Should be filled in by the implementation with
         * the information about the insets of the window.  This is called
         * with whatever values the previous OnComputeInternalInsetsListener
         * returned, if there are multiple such listeners in the window.
         */
        public void onComputeInternalInsets(InternalInsetsInfo inoutInfo);
    }

    /**
     * Creates a new ViewTreeObserver. This constructor should not be called
     */
    ViewTreeObserver() {
    }

    /**
     * Merges all the listeners registered on the specified observer with the listeners
     * registered on this object. After this method is invoked, the specified observer
     * will return false in {@link #isAlive()} and should not be used anymore.
     *
     * @param observer The ViewTreeObserver whose listeners must be added to this observer
     */
    void merge(ViewTreeObserver observer) {
        if (observer.mOnGlobalFocusListeners != null) {
            if (mOnGlobalFocusListeners != null) {
                mOnGlobalFocusListeners.addAll(observer.mOnGlobalFocusListeners);
            } else {
                mOnGlobalFocusListeners = observer.mOnGlobalFocusListeners;
            }
        }

        if (observer.mOnGlobalLayoutListeners != null) {
            if (mOnGlobalLayoutListeners != null) {
                mOnGlobalLayoutListeners.addAll(observer.mOnGlobalLayoutListeners);
            } else {
                mOnGlobalLayoutListeners = observer.mOnGlobalLayoutListeners;
            }
        }

        if (observer.mOnPreDrawListeners != null) {
            if (mOnPreDrawListeners != null) {
                mOnPreDrawListeners.addAll(observer.mOnPreDrawListeners);
            } else {
                mOnPreDrawListeners = observer.mOnPreDrawListeners;
            }
        }

        if (observer.mOnTouchModeChangeListeners != null) {
            if (mOnTouchModeChangeListeners != null) {
                mOnTouchModeChangeListeners.addAll(observer.mOnTouchModeChangeListeners);
            } else {
                mOnTouchModeChangeListeners = observer.mOnTouchModeChangeListeners;
            }
        }

        if (observer.mOnComputeInternalInsetsListeners != null) {
            if (mOnComputeInternalInsetsListeners != null) {
                mOnComputeInternalInsetsListeners.addAll(observer.mOnComputeInternalInsetsListeners);
            } else {
                mOnComputeInternalInsetsListeners = observer.mOnComputeInternalInsetsListeners;
            }
        }

        if (observer.mOnScrollChangedListeners != null) {
            if (mOnScrollChangedListeners != null) {
                mOnScrollChangedListeners.addAll(observer.mOnScrollChangedListeners);
            } else {
                mOnScrollChangedListeners = observer.mOnScrollChangedListeners;
            }
        }

        observer.kill();
    }

    /**
     * Register a callback to be invoked when the focus state within the view tree changes.
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {
        checkIsAlive();

        if (mOnGlobalFocusListeners == null) {
            mOnGlobalFocusListeners = new CopyOnWriteArrayList<OnGlobalFocusChangeListener>();
        }

        mOnGlobalFocusListeners.add(listener);
    }

    /**
     * Remove a previously installed focus change callback.
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener)
     */
    public void removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener victim) {
        checkIsAlive();
        if (mOnGlobalFocusListeners == null) {
            return;
        }
        mOnGlobalFocusListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when the global layout state or the visibility of views
     * within the view tree changes
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {
        checkIsAlive();

        if (mOnGlobalLayoutListeners == null) {
            mOnGlobalLayoutListeners = new CopyOnWriteArray<OnGlobalLayoutListener>();
        }

        mOnGlobalLayoutListeners.add(listener);
    }

    /**
     * Remove a previously installed global layout callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     * 
     * @deprecated Use #removeOnGlobalLayoutListener instead
     *
     * @see #addOnGlobalLayoutListener(OnGlobalLayoutListener)
     */
    @Deprecated
    public void removeGlobalOnLayoutListener(OnGlobalLayoutListener victim) {
        removeOnGlobalLayoutListener(victim);
    }

    /**
     * Remove a previously installed global layout callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     * 
     * @see #addOnGlobalLayoutListener(OnGlobalLayoutListener)
     */
    public void removeOnGlobalLayoutListener(OnGlobalLayoutListener victim) {
        checkIsAlive();
        if (mOnGlobalLayoutListeners == null) {
            return;
        }
        mOnGlobalLayoutListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when the view tree is about to be drawn
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnPreDrawListener(OnPreDrawListener listener) {
        checkIsAlive();

        if (mOnPreDrawListeners == null) {
            mOnPreDrawListeners = new CopyOnWriteArray<OnPreDrawListener>();
        }

        mOnPreDrawListeners.add(listener);
    }

    /**
     * Remove a previously installed pre-draw callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnPreDrawListener(OnPreDrawListener)
     */
    public void removeOnPreDrawListener(OnPreDrawListener victim) {
        checkIsAlive();
        if (mOnPreDrawListeners == null) {
            return;
        }
        mOnPreDrawListeners.remove(victim);
    }

    /**
     * <p>Register a callback to be invoked when the view tree is about to be drawn.</p>
     * <p><strong>Note:</strong> this method <strong>cannot</strong> be invoked from
     * {@link android.view.ViewTreeObserver.OnDrawListener#onDraw()}.</p>
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnDrawListener(OnDrawListener listener) {
        checkIsAlive();

        if (mOnDrawListeners == null) {
            mOnDrawListeners = new ArrayList<OnDrawListener>();
        }

        mOnDrawListeners.add(listener);
    }

    /**
     * <p>Remove a previously installed pre-draw callback.</p>
     * <p><strong>Note:</strong> this method <strong>cannot</strong> be invoked from
     * {@link android.view.ViewTreeObserver.OnDrawListener#onDraw()}.</p>
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnDrawListener(OnDrawListener)
     */
    public void removeOnDrawListener(OnDrawListener victim) {
        checkIsAlive();
        if (mOnDrawListeners == null) {
            return;
        }
        mOnDrawListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when a view has been scrolled.
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnScrollChangedListener(OnScrollChangedListener listener) {
        checkIsAlive();

        if (mOnScrollChangedListeners == null) {
            mOnScrollChangedListeners = new CopyOnWriteArray<OnScrollChangedListener>();
        }

        mOnScrollChangedListeners.add(listener);
    }

    /**
     * Remove a previously installed scroll-changed callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnScrollChangedListener(OnScrollChangedListener)
     */
    public void removeOnScrollChangedListener(OnScrollChangedListener victim) {
        checkIsAlive();
        if (mOnScrollChangedListeners == null) {
            return;
        }
        mOnScrollChangedListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when the invoked when the touch mode changes.
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     */
    public void addOnTouchModeChangeListener(OnTouchModeChangeListener listener) {
        checkIsAlive();

        if (mOnTouchModeChangeListeners == null) {
            mOnTouchModeChangeListeners = new CopyOnWriteArrayList<OnTouchModeChangeListener>();
        }

        mOnTouchModeChangeListeners.add(listener);
    }

    /**
     * Remove a previously installed touch mode change callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnTouchModeChangeListener(OnTouchModeChangeListener)
     */
    public void removeOnTouchModeChangeListener(OnTouchModeChangeListener victim) {
        checkIsAlive();
        if (mOnTouchModeChangeListeners == null) {
            return;
        }
        mOnTouchModeChangeListeners.remove(victim);
    }

    /**
     * Register a callback to be invoked when the invoked when it is time to
     * compute the window's internal insets.
     *
     * @param listener The callback to add
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        checkIsAlive();

        if (mOnComputeInternalInsetsListeners == null) {
            mOnComputeInternalInsetsListeners =
                    new CopyOnWriteArray<OnComputeInternalInsetsListener>();
        }

        mOnComputeInternalInsetsListeners.add(listener);
    }

    /**
     * Remove a previously installed internal insets computation callback
     *
     * @param victim The callback to remove
     *
     * @throws IllegalStateException If {@link #isAlive()} returns false
     *
     * @see #addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener)
     * 
     * We are not yet ready to commit to this API and support it, so
     * @hide
     */
    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener victim) {
        checkIsAlive();
        if (mOnComputeInternalInsetsListeners == null) {
            return;
        }
        mOnComputeInternalInsetsListeners.remove(victim);
    }

    private void checkIsAlive() {
        if (!mAlive) {
            throw new IllegalStateException("This ViewTreeObserver is not alive, call "
                    + "getViewTreeObserver() again");
        }
    }

    /**
     * Indicates whether this ViewTreeObserver is alive. When an observer is not alive,
     * any call to a method (except this one) will throw an exception.
     *
     * If an application keeps a long-lived reference to this ViewTreeObserver, it should
     * always check for the result of this method before calling any other method.
     *
     * @return True if this object is alive and be used, false otherwise.
     */
    public boolean isAlive() {
        return mAlive;
    }

    /**
     * Marks this ViewTreeObserver as not alive. After invoking this method, invoking
     * any other method but {@link #isAlive()} and {@link #kill()} will throw an Exception.
     *
     * @hide
     */
    private void kill() {
        mAlive = false;
    }

    /**
     * Notifies registered listeners that focus has changed.
     */
    final void dispatchOnGlobalFocusChange(View oldFocus, View newFocus) {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArrayList<OnGlobalFocusChangeListener> listeners = mOnGlobalFocusListeners;
        if (listeners != null && listeners.size() > 0) {
            for (OnGlobalFocusChangeListener listener : listeners) {
                listener.onGlobalFocusChanged(oldFocus, newFocus);
            }
        }
    }

    /**
     * Notifies registered listeners that a global layout happened. This can be called
     * manually if you are forcing a layout on a View or a hierarchy of Views that are
     * not attached to a Window or in the GONE state.
     */
    public final void dispatchOnGlobalLayout() {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArray<OnGlobalLayoutListener> listeners = mOnGlobalLayoutListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnGlobalLayoutListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onGlobalLayout();
                }
            } finally {
                listeners.end();
            }
        }
    }

    /**
     * Notifies registered listeners that the drawing pass is about to start. If a
     * listener returns true, then the drawing pass is canceled and rescheduled. This can
     * be called manually if you are forcing the drawing on a View or a hierarchy of Views
     * that are not attached to a Window or in the GONE state.
     *
     * @return True if the current draw should be canceled and resceduled, false otherwise.
     */
    @SuppressWarnings("unchecked")
    public final boolean dispatchOnPreDraw() {
        boolean cancelDraw = false;
        final CopyOnWriteArray<OnPreDrawListener> listeners = mOnPreDrawListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnPreDrawListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    cancelDraw |= !(access.get(i).onPreDraw());
                }
            } finally {
                listeners.end();
            }
        }
        return cancelDraw;
    }

    /**
     * Notifies registered listeners that the drawing pass is about to start.
     */
    public final void dispatchOnDraw() {
        if (mOnDrawListeners != null) {
            final ArrayList<OnDrawListener> listeners = mOnDrawListeners;
            int numListeners = listeners.size();
            for (int i = 0; i < numListeners; ++i) {
                listeners.get(i).onDraw();
            }
        }
    }

    /**
     * Notifies registered listeners that the touch mode has changed.
     *
     * @param inTouchMode True if the touch mode is now enabled, false otherwise.
     */
    final void dispatchOnTouchModeChanged(boolean inTouchMode) {
        final CopyOnWriteArrayList<OnTouchModeChangeListener> listeners =
                mOnTouchModeChangeListeners;
        if (listeners != null && listeners.size() > 0) {
            for (OnTouchModeChangeListener listener : listeners) {
                listener.onTouchModeChanged(inTouchMode);
            }
        }
    }

    /**
     * Notifies registered listeners that something has scrolled.
     */
    final void dispatchOnScrollChanged() {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArray<OnScrollChangedListener> listeners = mOnScrollChangedListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnScrollChangedListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onScrollChanged();
                }
            } finally {
                listeners.end();
            }
        }
    }

    /**
     * Returns whether there are listeners for computing internal insets.
     */
    final boolean hasComputeInternalInsetsListeners() {
        final CopyOnWriteArray<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        return (listeners != null && listeners.size() > 0);
    }

    /**
     * Calls all listeners to compute the current insets.
     */
    final void dispatchOnComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        // NOTE: because of the use of CopyOnWriteArrayList, we *must* use an iterator to
        // perform the dispatching. The iterator is a safe guard against listeners that
        // could mutate the list by calling the various add/remove methods. This prevents
        // the array from being modified while we iterate it.
        final CopyOnWriteArray<OnComputeInternalInsetsListener> listeners =
                mOnComputeInternalInsetsListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnComputeInternalInsetsListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onComputeInternalInsets(inoutInfo);
                }
            } finally {
                listeners.end();
            }
        }
    }

    /**
     * Copy on write array. This array is not thread safe, and only one loop can
     * iterate over this array at any given time. This class avoids allocations
     * until a concurrent modification happens.
     * 
     * Usage:
     * 
     * CopyOnWriteArray.Access<MyData> access = array.start();
     * try {
     *     for (int i = 0; i < access.size(); i++) {
     *         MyData d = access.get(i);
     *     }
     * } finally {
     *     access.end();
     * }
     */
    static class CopyOnWriteArray<T> {
        private ArrayList<T> mData = new ArrayList<T>();
        private ArrayList<T> mDataCopy;

        private final Access<T> mAccess = new Access<T>();

        private boolean mStart;

        static class Access<T> {
            private ArrayList<T> mData;
            private int mSize;

            T get(int index) {
                return mData.get(index);
            }

            int size() {
                return mSize;
            }
        }

        CopyOnWriteArray() {
        }

        private ArrayList<T> getArray() {
            if (mStart) {
                if (mDataCopy == null) mDataCopy = new ArrayList<T>(mData);
                return mDataCopy;
            }
            return mData;
        }

        Access<T> start() {
            if (mStart) throw new IllegalStateException("Iteration already started");
            mStart = true;
            mDataCopy = null;
            mAccess.mData = mData;
            mAccess.mSize = mData.size();
            return mAccess;
        }

        void end() {
            if (!mStart) throw new IllegalStateException("Iteration not started");
            mStart = false;
            if (mDataCopy != null) {
                mData = mDataCopy;
            }
            mDataCopy = null;
        }

        int size() {
            return getArray().size();
        }

        void add(T item) {
            getArray().add(item);
        }

        void addAll(CopyOnWriteArray<T> array) {
            getArray().addAll(array.mData);
        }

        void remove(T item) {
            getArray().remove(item);
        }

        void clear() {
            getArray().clear();
        }
    }
}