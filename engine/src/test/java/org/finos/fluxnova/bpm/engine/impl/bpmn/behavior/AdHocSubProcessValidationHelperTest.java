package org.finos.fluxnova.bpm.engine.impl.bpmn.behavior;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.finos.fluxnova.bpm.engine.ActivityTypes;
import org.finos.fluxnova.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.finos.fluxnova.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.finos.fluxnova.bpm.engine.impl.pvm.process.ActivityImpl;
import org.finos.fluxnova.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.junit.Test;

public class AdHocSubProcessValidationHelperTest {

  @Test
  public void testIsStartableActivityTypeForWhitelistedTypes() {
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_SCRIPT));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_SERVICE));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_BUSINESS_RULE));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_MANUAL_TASK));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_USER_TASK));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_SEND_TASK));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.TASK_RECEIVE_TASK));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.CALL_ACTIVITY));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.SUB_PROCESS));
    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.SUB_PROCESS_AD_HOC));
  }

  @Test
  public void testIsStartableActivityTypeRejectsNullAndNonWhitelistedTypes() {
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(null));
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.START_EVENT));
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.END_EVENT_NONE));
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.GATEWAY_EXCLUSIVE));
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.BOUNDARY_TIMER));
    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityType(ActivityTypes.INTERMEDIATE_EVENT_CATCH));
  }

  @Test
  public void testIsStartableActivityInAdHocScopeReturnsTrueForWhitelistedTypeWithoutIncomingFromScope() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl("process");
    ActivityImpl adHocScope = processDefinition.createActivity("adHocScope");
    ActivityImpl activity = adHocScope.createActivity("activity");
    activity.setProperty(BpmnProperties.TYPE.getName(), ActivityTypes.TASK_USER_TASK);

    assertTrue(AdHocSubProcessValidationHelper.isStartableActivityInAdHocScope(adHocScope, activity));
  }

  @Test
  public void testIsStartableActivityInAdHocScopeRejectsCompensationHandlers() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl("process");
    ActivityImpl adHocScope = processDefinition.createActivity("adHocScope");
    ActivityImpl activity = adHocScope.createActivity("activity");
    activity.setProperty(BpmnProperties.TYPE.getName(), ActivityTypes.TASK_USER_TASK);
    activity.setProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION, true);

    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityInAdHocScope(adHocScope, activity));
  }

  @Test
  public void testIsStartableActivityInAdHocScopeRejectsIncomingTransitionFromSameScope() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl("process");
    ActivityImpl adHocScope = processDefinition.createActivity("adHocScope");
    ActivityImpl source = adHocScope.createActivity("source");
    ActivityImpl target = adHocScope.createActivity("target");

    source.setProperty(BpmnProperties.TYPE.getName(), ActivityTypes.TASK_USER_TASK);
    target.setProperty(BpmnProperties.TYPE.getName(), ActivityTypes.TASK_USER_TASK);

    source.createOutgoingTransition("flow").setDestination(target);

    assertFalse(AdHocSubProcessValidationHelper.isStartableActivityInAdHocScope(adHocScope, target));
  }

  @Test
  public void testHasIncomingTransitionFromAdHocScopeIgnoresExternalIncomingTransitions() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl("process");
    ActivityImpl adHocScope = processDefinition.createActivity("adHocScope");
    ActivityImpl externalSource = processDefinition.createActivity("externalSource");
    ActivityImpl target = adHocScope.createActivity("target");

    externalSource.createOutgoingTransition("flow").setDestination(target);

    assertFalse(AdHocSubProcessValidationHelper.hasIncomingTransitionFromAdHocScope(adHocScope, target));
  }
}
