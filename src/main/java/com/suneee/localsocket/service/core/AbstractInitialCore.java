package com.suneee.localsocket.service.core;

/**
 * Created by QueRenJie on ${date}
 */
public abstract class AbstractInitialCore {
    public final void build() {
        this.buildConfig();
        this.startListener();
    }

    protected abstract void buildConfig();

    protected abstract void startListener();

}
