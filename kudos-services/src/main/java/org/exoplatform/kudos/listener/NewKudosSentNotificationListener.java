package org.exoplatform.kudos.listener;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_DETAILS_PARAMETER;
import static org.exoplatform.kudos.service.utils.Utils.KUDOS_RECEIVER_NOTIFICATION_ID;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * A listener to send notification after sending a new Kudos
 */
public class NewKudosSentNotificationListener extends Listener<KudosService, Kudos> {
  private static final Log LOG = ExoLogger.getLogger(NewKudosSentNotificationListener.class);

  @Override
  public void onEvent(Event<KudosService, Kudos> event) throws Exception {
    Kudos kudos = event.getData();
    try {
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.append(KUDOS_DETAILS_PARAMETER, kudos);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(KUDOS_RECEIVER_NOTIFICATION_ID))).execute(ctx);
    } catch (Exception e) {
      LOG.warn("Error sending notification for Kudos with id " + kudos.getTechnicalId(), e);
    }
  }
}
