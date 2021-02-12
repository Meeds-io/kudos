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
    private String RESOURCE_BUNDLE_VALUES_PARAM = "RESOURCE_BUNDLE_VALUES_PARAM";


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
            String activityTitle= activity.getTitle().split(":")[1].split("<")[0];
            Map<String, String> templateParams = activity.getTemplateParams();
            String  resourve_bundle_values_param= templateParams.get(RESOURCE_BUNDLE_VALUES_PARAM) ;
            String[] activityParamValues = getParamValues(resourve_bundle_values_param);
            activityParamValues[2]=": " +activityTitle;
            StringBuilder new_resourve_bundle_values_param = new StringBuilder().append(activityParamValues[0])
                    .append("#").append(activityParamValues[1]).append("#")
                    .append(activityParamValues[2]).append("#")
                    .append(activityParamValues[3]);
            templateParams.put(RESOURCE_BUNDLE_VALUES_PARAM,new_resourve_bundle_values_param.toString());
            activity.setTemplateParams(templateParams);
            KudosEntity kudos = kudosService.getKudosByActivityId(Long.parseLong(activity.getId()));
            kudos.setMessage(activityTitle);
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
