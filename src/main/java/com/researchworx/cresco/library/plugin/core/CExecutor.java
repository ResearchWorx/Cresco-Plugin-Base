package com.researchworx.cresco.library.plugin.core;

import com.researchworx.cresco.library.messaging.MsgEvent;

public abstract class CExecutor {
    public CExecutor() {

    }

    public abstract MsgEvent process(MsgEvent incoming);
}
