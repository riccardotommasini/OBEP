package it.polimi.deib.sr.obep.core.programming;

import com.espertech.esper.client.StatementAwareUpdateListener;

import java.util.Observer;

public interface ResultFormatter extends Observer, StatementAwareUpdateListener {
}
