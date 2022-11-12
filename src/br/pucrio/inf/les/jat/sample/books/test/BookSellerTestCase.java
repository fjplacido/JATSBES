package br.pucrio.inf.les.jat.sample.books.test;

import br.pucrio.inf.les.jat.aspects.monitor.AgentMonitorServices;
import br.pucrio.inf.les.jat.core.JadeTestCase;

public class BookSellerTestCase extends JadeTestCase {

	private static final String BOOK_QUANTITY = "2";
	private static final String BOOK = "Programação Modular";
	private static final String BOOK_SELLER = "BookSeller";
	private static final String FIRST_BUYER = "FirstBuyer";
	private static final String SECOND_BUYER = "SecondBuyer";

	public void testDoisCompradoresUmLivro() {

		registerAndStartAgent(BOOK_SELLER, "br.pucrio.inf.les.jat.sample.books.BookSellerAgent",
				new Object[] { BOOK, BOOK_QUANTITY });
		registerAndStartMockAgent(FIRST_BUYER, "br.pucrio.inf.les.jat.sample.books.MockFirstBuyer", new Object[] {});
		registerAndStartMockAgent(SECOND_BUYER, "br.pucrio.inf.les.jat.sample.books.MockSecoundBuyer",
				new Object[] {});

		AgentMonitorServices.waitUntilTestHasFinished(FIRST_BUYER);
		AgentMonitorServices.waitUntilTestHasFinished(SECOND_BUYER);

		assertMockAgent(FIRST_BUYER);
		assertMockAgent(SECOND_BUYER);
	}
}