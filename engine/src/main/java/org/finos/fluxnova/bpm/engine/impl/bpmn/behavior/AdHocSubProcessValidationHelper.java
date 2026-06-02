package org.finos.fluxnova.bpm.engine.impl.bpmn.behavior;

import java.util.Set;
import org.finos.fluxnova.bpm.engine.ActivityTypes;
import org.finos.fluxnova.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.finos.fluxnova.bpm.engine.impl.pvm.PvmTransition;
import org.finos.fluxnova.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Shared validation utilities for ad-hoc subprocess activity filtering and authorization.
 * Centralizes startable activity type whitelist and scope boundary checks used by both
 * {@link AdHocSubProcessActivityBehavior} and the trigger command.
 */
public class AdHocSubProcessValidationHelper {

  protected static final Set<String> STARTABLE_AD_HOC_ACTIVITY_TYPES = Set.of(
      ActivityTypes.TASK,
      ActivityTypes.TASK_SCRIPT,
      ActivityTypes.TASK_SERVICE,
      ActivityTypes.TASK_BUSINESS_RULE,
      ActivityTypes.TASK_MANUAL_TASK,
      ActivityTypes.TASK_USER_TASK,
      ActivityTypes.TASK_SEND_TASK,
      ActivityTypes.TASK_RECEIVE_TASK,
      ActivityTypes.CALL_ACTIVITY,
      ActivityTypes.SUB_PROCESS,
      ActivityTypes.SUB_PROCESS_AD_HOC
  );

  private AdHocSubProcessValidationHelper() {
    // Static utility class
  }

  /**
   * Checks if an activity type is allowed for ad-hoc startup.
   * Only task activity types are startable by default.
   *
   * @param type the BPMN activity type
   * @return true if the type is startable in ad-hoc scope
   */
  public static boolean isStartableActivityType(String type) {
    return type != null && STARTABLE_AD_HOC_ACTIVITY_TYPES.contains(type);
  }

  /**
   * Checks if an activity has an incoming transition from within the ad-hoc scope.
   * Activities with such transitions are downstream-only and cannot be starter activities.
   *
   * @param adHocScope the ad-hoc subprocess scope
   * @param activity the activity to check
   * @return true if activity has incoming transition from within scope
   */
  public static boolean hasIncomingTransitionFromAdHocScope(ActivityImpl adHocScope, ActivityImpl activity) {
    for (PvmTransition incomingTransition : activity.getIncomingTransitions()) {
      if (incomingTransition.getSource() instanceof ActivityImpl) {
        ActivityImpl source = (ActivityImpl) incomingTransition.getSource();
        if (adHocScope.findActivity(source.getId()) != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determines if an activity can be started within an ad-hoc scope.
   * Rejects compensation handlers, non-startable activity types, and activities
   * with incoming transitions from within the scope.
   *
   * @param adHocScope the ad-hoc subprocess scope
   * @param activity the activity to validate
   * @return true if activity is startable in ad-hoc scope
   */
  public static boolean isStartableActivityInAdHocScope(ActivityImpl adHocScope, ActivityImpl activity) {
    if (activity.isCompensationHandler()) {
      return false;
    }

    if (ActivityTypes.SUB_PROCESS.equals(activity.getProperty(BpmnProperties.TYPE.getName()))
        && Boolean.TRUE.equals(activity.getProperty(BpmnProperties.TRIGGERED_BY_EVENT.getName()))) {
      return false;
    }

    String type = (String) activity.getProperty(BpmnProperties.TYPE.getName());
    if (!isStartableActivityType(type)) {
      return false;
    }

    return !hasIncomingTransitionFromAdHocScope(adHocScope, activity);
  }
}
