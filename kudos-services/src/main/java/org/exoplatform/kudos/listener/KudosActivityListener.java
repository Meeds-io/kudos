package org.exoplatform.kudos.listener;

import lombok.SneakyThrows;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.kudos.dao.KudosDAO;
import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.model.Kudos;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.kudos.service.KudosStorage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

import java.util.Map;

public class KudosActivityListener extends ActivityListenerPlugin {
    private static final Log LOG = ExoLogger.getLogger(KudosActivityListener.class);
    private static String activityType ="exokudos:activity";
    private KudosDAO kudosDAO;
    private KudosService kudosService;

    public KudosActivityListener(KudosDAO kudosDAO, KudosService kudosService) {
        this.kudosDAO = kudosDAO;
        this.kudosService = kudosService;
    }

    @Override
    public void saveActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    }


    @Override
    public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
        ExoSocialActivity activity = (ExoSocialActivity)activityLifeCycleEvent.getSource();
        String type = activity.getType();
        if (activity.getType().equals(activityType) ){
            String msg= activity.getTitle().split(":")[1].split("<")[0];
            Map<String, String> templateParams = activity.getTemplateParams();
            String RESOURCE_BUNDLE_VALUES_PARAM = templateParams.get("RESOURCE_BUNDLE_VALUES_PARAM") ;
            String[] toChange = RESOURCE_BUNDLE_VALUES_PARAM.split("#");
            toChange[2]=": " +msg;
            RESOURCE_BUNDLE_VALUES_PARAM=toChange[0]+'#'+toChange[1]+'#'+toChange[2]+"#"+toChange[3];
            templateParams.put("RESOURCE_BUNDLE_VALUES_PARAM",RESOURCE_BUNDLE_VALUES_PARAM);
            activity.setTemplateParams(templateParams);
            KudosEntity kudos = kudosService.getKudosByActivityId(Long.parseLong(activity.getId()));
            kudos.setMessage(msg);
            kudosDAO.update(kudos);
        }
    }

    @Override
    public void saveComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
    }

    @Override
    public void updateComment(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }

    @Override
    public void likeActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }

    @Override
    public void likeComment(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }
}
