package org.pgi.paxoscoin.events;

import java.io.Serializable;
import java.time.Instant;

public interface Event extends Serializable {

    public Instant getTime();
    
}
