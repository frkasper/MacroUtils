package simplehexamesher;

import star.assistant.Task;
import star.assistant.annotation.StarAssistantTask;

/**
 * Q&A Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        contentPath = "html/QuestionsAndAnswersTask.xhtml",
        display = "Q&A"
)
public class QuestionsAndAnswersTask extends Task { }
