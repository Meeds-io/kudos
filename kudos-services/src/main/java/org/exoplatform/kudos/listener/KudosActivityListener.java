package org.exoplatform.kudos.listener;

import org.exoplatform.kudos.entity.KudosEntity;
import org.exoplatform.kudos.service.KudosService;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import java.util.Map;

import static org.exoplatform.kudos.service.utils.Utils.KUDOS_ACTIVITY_COMMENT_TYPE;
import static org.exoplatform.social.core.processor.I18NActivityUtils.getParamValues;

public class KudosActivityListener extends ActivityListenerPlugin {
    private KudosService kudosService;

    public KudosActivityListener( KudosService kudosService) {
        this.kudosService = kudosService;
    }

    @Override
    public void saveActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
    }


    @Override
    public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
        ExoSocialActivity activity = (ExoSocialActivity)activityLifeCycleEvent.getSource();
        if (activity.getType().equals(KUDOS_ACTIVITY_COMMENT_TYPE)){
            String msg= activity.getTitle().split(":")[1].split("<")[0];
            Map<String, String> templateParams = activity.getTemplateParams();
            String  resourve_bundle_values_param= templateParams.get("RESOURCE_BUNDLE_VALUES_PARAM") ;
            String[] toChange = getParamValues(resourve_bundle_values_param);
            toChange[2]=": " +msg;
            StringBuilder new_resourve_bundle_values_param = new StringBuilder().append(toChange[0])
                    .append("#").append(toChange[1]).append("#")
                    .append(toChange[2]).append("#")
                    .append(toChange[3]);
            templateParams.put("RESOURCE_BUNDLE_VALUES_PARAM",new_resourve_bundle_values_param.toString());
            activity.setTemplateParams(templateParams);
            KudosEntity kudos = kudosService.getKudosByActivityId(Long.parseLong(activity.getId()));
            kudos.setMessage(msg);
            kudosService.updateKudos(kudos);
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
