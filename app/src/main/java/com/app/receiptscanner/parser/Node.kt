package com.app.receiptscanner.parser

data class Node(val content: ArrayList<String>, val parentNode: Node?, val childNodes: ArrayList<Node>,val isLeaf: Boolean)

