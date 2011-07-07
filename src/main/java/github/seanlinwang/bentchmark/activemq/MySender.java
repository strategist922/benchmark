package github.seanlinwang.bentchmark.activemq;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * mq sender
 */
public class MySender implements Runnable {

	private JmsTemplate template;
	private Destination destination;
	private String[] resultSet;

	public MySender(int times) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext.xml");
		template = (JmsTemplate) ctx.getBean("jmsTemplate");
		template.setDeliveryMode(DeliveryMode.PERSISTENT);
		template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
		template.setDeliveryPersistent(true);
		destination = (Destination) ctx.getBean("destination");

		this.resultSet = new String[times];
	}

	@Override
	public void run() {
		for (int i = 0; i < resultSet.length; i++) {
			final long now = System.currentTimeMillis();
			template.send(destination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					TextMessage message = session.createTextMessage("" + now);
					message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
					return message;
				}
			});
			long end = System.currentTimeMillis();
			resultSet[i] = ((end - now) + "," + end / 1000);
		}
	}

	public static void main(String[] args) throws JMSException, IOException, InterruptedException {
		MySender[] senders = new MySender[20];
		for (int i = 0; i < senders.length; i++) {
			senders[i] = new MySender(1000);
		}
		Thread[] ts = new Thread[senders.length];
		for (int i = 0; i < senders.length; i++) {
			Thread t = new Thread(senders[i]);
			ts[i] = t;
			t.start();
		}

		for (int i = 0; i < ts.length; i++) {
			ts[i].join();
		}

		FileWriter outFile = new FileWriter("C:\\Users\\Yao\\Documents\\mq-sender-test.csv");
		PrintWriter out = new PrintWriter(outFile);

		for (int i = 0; i < senders.length; i++) {
			MySender sender = senders[i];
			sender.print(out);
		}

		out.close();

	}

	private void print(PrintWriter out2) {
		for (String line : resultSet) {
			out2.write(line + "\n");
		}
	}

}
