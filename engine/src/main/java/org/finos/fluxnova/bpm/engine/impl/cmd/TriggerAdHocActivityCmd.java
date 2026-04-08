package org.finos.fluxnova.bpm.engine.impl.cmd;

import static org.finos.fluxnova.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import org.finos.fluxnova.bpm.engine.ActivityTypes;
import org.finos.fluxnova.bpm.engine.BadUserRequestException;
import org.finos.fluxnova.bpm.engine.impl.bpmn.behavior.AdHocSubProcessActivityBehavior;
import org.finos.fluxnova.bpm.engine.impl.bpmn.behavior.AdHocSubProcessValidationHelper;
import org.finos.fluxnova.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.finos.fluxnova.bpm.engine.impl.cfg.CommandChecker;
import org.finos.fluxnova.bpm.engine.impl.interceptor.Command;
import org.finos.fluxnova.bpm.engine.impl.interceptor.CommandContext;
import org.finos.fluxnova.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.finos.fluxnova.bpm.engine.impl.pvm.PvmTransition;
import org.finos.fluxnova.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.finos.fluxnova.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Triggers a specific starter activity inside an active ad-hoc subprocess execution.
 */
public class TriggerAdHocActivityCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final String executionId;
  protected final String activityId;

  public TriggerAdHocActivityCmd(String executionId, String activityId) {
    this.executionId = executionId;
    this.activityId = activityId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "executionId is null", "executionId", executionId);
    ensureNotNull(BadUserRequestException.class, "activityId is null", "activityId", activityId);

    ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(executionId);
    ensureNotNull(BadUserRequestException.class, "execution " + executionId + " doesn't exist", "execution", execution);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }

    ActivityImpl adHocActivity = execution.getActivity();
    ensureNotNull(BadUserRequestException.class, "execution " + executionId + " has no current activity", "activity", adHocActivity);

    if (!(adHocActivity.getActivityBehavior() instanceof AdHocSubProcessActivityBehavior)) {
      throw new BadUserRequestException("execution " + executionId + " is not waiting in an adHocSubProcess");
    }

    ActivityImpl targetActivity = adHocActivity.findActivity(activityId);
    ensureNotNull(BadUserRequestException.class,
        "adHoc activity '" + activityId + "' does not exist in adHocSubProcess " + adHocActivity.getId(),
        "targetActivity",
        targetActivity);

    if (!isStartableInAdHocScope(adHocActivity, targetActivity)) {
      throw new BadUserRequestException(
          "adHoc activity '" + activityId + "' is not startable in adHocSubProcess " + adHocActivity.getId());
    }

    boolean alreadyActive = execution.getExecutions().stream()
      .anyMatch(child -> child.getActivity() != null
        && activityId.equals(child.getActivity().getId())
        && child.isActive());

    if (alreadyActive) {
      throw new BadUserRequestException("adHoc activity '" + activityId + "' is already active in adHocSubProcess " + adHocActivity.getId());
    }

    ActivityExecution childExecution = execution.createConcurrentExecution();
    childExecution.executeActivity(targetActivity);

    return null;
  }

  protected boolean isStartableInAdHocScope(ActivityImpl adHocScope, ActivityImpl activity) {
    return AdHocSubProcessValidationHelper.isStartableActivityInAdHocScope(adHocScope, activity);
  }

  protected boolean hasIncomingTransitionFromAdHocScope(ActivityImpl adHocScope, ActivityImpl activity) {
    return AdHocSubProcessValidationHelper.hasIncomingTransitionFromAdHocScope(adHocScope, activity);
  }
}
