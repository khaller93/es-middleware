package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This class represents an util for handling {@link SPARQLDAOUpdatedEvent}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class NotificationScheduler {

  private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

  private Timer timer = new Timer();
  private Lock notifyLock = new ReentrantLock();

  private long updateInterval;
  private long updateIntervalTimeout;

  private ApplicationEventPublisher eventPublisher;
  private NotificationTask notificationTask;

  NotificationScheduler(ApplicationEventPublisher eventPublisher,
      long updateInterval, long updateIntervalTimeout) {
    this.eventPublisher = eventPublisher;
    this.updateInterval = updateInterval;
    this.updateIntervalTimeout = updateIntervalTimeout;
  }

  /**
   * This method shall be called if an update should be recognized.
   */
  void updated() {
    notifyLock.lock();
    try {
      if (notificationTask == null) {
        notificationTask = new NotificationTask(Instant.now());
        timer.schedule(notificationTask, updateInterval);
      } else {
        notificationTask.update(Instant.now());
      }
    } finally {
      notifyLock.unlock();
    }
  }

  private final class NotificationTask extends TimerTask {

    private Instant initTimestamp;
    private Instant lastCallTimestamp;

    public NotificationTask(Instant initTimestamp) {
      this.initTimestamp = initTimestamp;
    }

    public void update(Instant now) {
      lastCallTimestamp = now;
    }

    @Override
    public void run() {
      Instant now = Instant.now();
      if (initTimestamp.plusMillis(updateIntervalTimeout).isBefore(now)) {
        publishUpdate(now);
      } else if (lastCallTimestamp == null) {
        publishUpdate(now);
      } else {
        timer.schedule(this, Date.from(lastCallTimestamp.plusMillis(updateInterval)));
      }
    }
  }

  private void publishUpdate(Instant now) {
    notifyLock.lock();
    try {
      logger.debug("An update event of the SPARQL DAO is published for timestamp {}.", now);
      eventPublisher.publishEvent(new SPARQLDAOUpdatedEvent(NotificationScheduler.this, now));
      notificationTask = null;
    } finally {
      notifyLock.unlock();
    }
  }

}
