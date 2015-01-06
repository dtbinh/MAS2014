package mas.agent.student;

import cz.agents.alite.communication.content.Content;

@SuppressWarnings("serial")
public class MyStringContent extends Content {
    String content;

    public MyStringContent(String content) {
        super(content);
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

}
