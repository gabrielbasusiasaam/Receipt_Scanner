package com.app.receiptscanner.database

import com.app.receiptscanner.parser.Parser
import com.app.receiptscanner.parser.Token
import com.app.receiptscanner.parser.Token.Companion.TYPE_DATA
import com.app.receiptscanner.parser.Token.Companion.TYPE_FIELD
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_AFTER
import com.app.receiptscanner.parser.TokenRelation.Companion.CHECK_BEFORE
import com.app.receiptscanner.parser.TokenRelation.TokenRelationsBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class ParserTest {
    //region Tests for the tokenizer
    @Test
    fun fullReceiptTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("TOTAL", 1, CHECK_AFTER)
            .addKeyWordRelation("CARD", 1, CHECK_AFTER)
            .addKeyWordRelation("Date", 1, CHECK_AFTER)
            .addKeyWordRelation("Time", 3, CHECK_AFTER)
            .addKeyWordRelation("MID", 1, CHECK_AFTER)
            .addKeyWordRelation("TID", 1, CHECK_AFTER)
            .addKeyWordRelation("TRNS", 2, CHECK_AFTER)
            .addKeyWordRelation("Visa", 2, CHECK_AFTER)
            .addKeyWordRelation("Amount", 1, CHECK_AFTER)
            .addKeyWordRelation(arrayListOf("AUTH", "CODE"), 1, CHECK_AFTER)
            .addRegexRelation("\\d+\\.\\d+A", -1, CHECK_BEFORE)
            .build()

        val parser = Parser(tokenRelations)
        val testData = """
    						Peckham
					VAT NO. GB350396892

		Cookie Hazelnut										0.99A
        Plain Brioche Buns									1.09A
        Southern Fried Steak								2.99A
    	----------------------------------------------------------
    	TOTAL												5.07
        CARD												 5.07
        ----------------------------------------------------------
		*CUSTOMER COPY* - PLEASE RETAIN RECEIPT
        Date: 25/01/23								Time: 15:18:13
        MID: ***10192								 TID: ****1211
        TRNS NO: UK061574013831025231
        Visa Debit								  ****************
        A0000000031010
        Contactless											  SALE
        Amount £5.07
        Verification Not Required
        APPROVED								  AUTH CODE 998388
        PLEASE DEBIT ACCOUNT WITH TOTAL SHOWN
        ----------------------------------------------------------

        VAT RATE				SALES £						 VAT £
        A 	  0%				 	5.07					 0.00
        ----------------------------------------------------------
        |			     Download the Lidl Plus app			     |
        |			     to save on your next shop			     |
        ----------------------------------------------------------









		0615	013831/74						25.01.23	 15:17
        	Enter survey: lidl.co.uk/haveyoursay
            & you can win £100 of Lidl Vouchers

        """
        assertEquals(parser.testTokenize(testData).toString(), "")
    }

    @Test
    fun fieldWithDataTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("Total", 1, CHECK_AFTER)
            .build()

        val testData = "Total: £1.00"
        val parser = Parser(tokenRelations)
        val expected = arrayListOf(Pair("TOTAL", TYPE_FIELD), Pair("£1.00", TYPE_DATA))
        val result = parser.testTokenize(testData)

        expected.forEachIndexed { index, content ->
            assertEquals(content.first, result[index].content.firstOrNull())
            assertEquals(content.second, result[index].type)
        }
    }

    @Test
    fun inputWithoutFieldsTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("Total", 1, CHECK_AFTER)
            .build()

        val testData = "5.07, 1.33"
        val parser = Parser(tokenRelations)
        val expected = arrayListOf(Pair("5.07", TYPE_DATA), Pair("1.33", TYPE_DATA))
        val result = parser.testTokenize(testData)

        expected.forEachIndexed { index, content ->
            assertEquals(content.first, result[index].content.firstOrNull())
            assertEquals(content.second, result[index].type)
        }
    }

    @Test
    fun emptyStringTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("Total", 1, CHECK_AFTER)
            .build()

        val testData = ""
        val parser = Parser(tokenRelations)
        val expected = arrayListOf<Token>()
        val result = parser.testTokenize(testData)

        assertEquals(expected, result)
    }
    //endregion

    //region Tests for the AST creator

    @Test
    fun fieldsAndDataTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("MID", 1, CHECK_AFTER)
            .addKeyWordRelation("TID", 1, CHECK_AFTER)
            .build()
        val expectedTokens = arrayListOf(
            Token(TYPE_FIELD, arrayListOf("MID"), 0, tokenRelations[0]),
            Token(TYPE_DATA, arrayListOf("10192"), 0),
            Token(TYPE_FIELD, arrayListOf("TID"), 0, tokenRelations[1]),
            Token(TYPE_DATA, arrayListOf("1211"), 0)
        )

        val parser = Parser(tokenRelations)
        val root = parser.createTestSyntaxTree(expectedTokens)
        root.childNodes.forEachIndexed { index, field ->
            assertEquals(expectedTokens[2 * index].content, field.content)
            assertEquals(expectedTokens[2 * index + 1].content, field.childNodes.first().content)
        }
    }

    @Test
    fun emptyInputTest() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("MID", 1, CHECK_AFTER)
            .addKeyWordRelation("TID", 1, CHECK_AFTER)
            .build()

        val emptyTokens = arrayListOf<Token>()

        val parser = Parser(tokenRelations)
        val root = parser.createTestSyntaxTree(emptyTokens)

        assert(root.childNodes.isEmpty())
    }

    @Test
    fun tokenizeAndAST() {
        val tokenRelations = TokenRelationsBuilder()
            .addKeyWordRelation("TOTAL", 1, CHECK_AFTER)
            .addKeyWordRelation("CARD", 1, CHECK_AFTER)
            .build()

        val testData =
            """
            TOTAL                                                  5.07
            CARD                                                  5.07
            """

        val expectedTokens = arrayListOf(
            Token(TYPE_FIELD, arrayListOf("TOTAL"), 0, tokenRelations[0]),
            Token(TYPE_DATA, arrayListOf("5.07"), 0),
            Token(TYPE_FIELD, arrayListOf("CARD"), 1, tokenRelations[1]),
            Token(TYPE_DATA, arrayListOf("5.07"), 1)
        )
        val parser = Parser(tokenRelations)
        val tokens = parser.testTokenize(testData)
        val root = parser.createTestSyntaxTree(tokens)
        assertEquals(expectedTokens, tokens)
        root.childNodes.forEachIndexed { index, field ->
            assertEquals(expectedTokens[2 * index].content, field.content)
            assertEquals(expectedTokens[2 * index + 1].content, field.childNodes.first().content)
        }
    }

    @Test
    fun inputOnlyData() {
        val tokens = arrayListOf(
            Token(TYPE_DATA, arrayListOf("£1.99"), 0),
            Token(TYPE_DATA, arrayListOf("3.44"), 0),
            Token(TYPE_DATA, arrayListOf("9.10"), 0)
        )

        val parser = Parser(listOf())
        val root = parser.createTestSyntaxTree(tokens)

        assert(root.childNodes.isEmpty())
    }

    //endregion
}
