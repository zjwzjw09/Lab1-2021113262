import java.io.*;
import java.nio.file.Files;
import java.util.*;


public class Graph {
	public static void main(String[] args) {
		Graph graph = new Graph();
		Scanner scanner = new Scanner(System.in);

		// 提示用户输入文件名
		String filename;
		File file;
		do {
			System.out.print("请输入文件名（以.txt结尾）：");
			filename = scanner.next();
			file = new File(filename);
			if (!file.exists()) {
				System.out.println("文件不存在，请重新输入。");
			}
		} while (!file.exists());

		// 创建有向图并展示
		graph.createGraph(filename);
		graph.showGraph();

		boolean exit = false;
		while (!exit) {
			// 显示菜单
			System.out.println("1. 查询桥接词");
			System.out.println("2. 根据桥接词生成新文本");
			System.out.println("3. 两个单词之间的最短路径");
			System.out.println("4. 到任一单词的最短路径");
			System.out.println("5. 随机游走");
			System.out.println("6. 退出");
			System.out.print("请选择要执行的操作：");

			// 用户选择操作
			String input = scanner.next();
			switch (input) {
				case "1":
					// 查询桥接词
					System.out.print("请输入两个单词（以空格隔开）：");
					String startWord = scanner.next();
					String endWord = scanner.next();
					graph.queryBridgeWords(startWord, endWord);
					break;
				case "2":
					// 根据桥接词生成新文本
					System.out.print("请输入要转换的文本：");
					scanner.nextLine(); // 清空输入缓冲区
					String inText = scanner.nextLine();
					System.out.println(graph.generateNewText(inText));
					break;
				case "3":
					// 查找两个单词之间的最短路径
					System.out.print("请输入两个单词（以空格隔开）：");
					startWord = scanner.next();
					endWord = scanner.next();
					graph.calcShortestPath(startWord, endWord);
					break;
				case "4":
					// 查找某个单词到任一单词的最短路径
					System.out.print("请输入起始单词：");
					startWord = scanner.next();
					graph.calcShortestPath(startWord);
					break;
				case "5":
					// 随机游走
					graph.randomWalk(scanner);
					break;
				case "6":
					// 退出程序
					exit = true;
					break;
				default:
					System.out.println("无效输入。");
					break;
			}
			if (exit) {
				System.exit(0);
				;
			}
		}
	}
	private static final int DEFAULT_ARRAY_SIZE = 1000; // 默认数组大小
	private List<String> words;  // 单词列表
	private Map<String, Integer> wordMap;  // 将单词映射为编号
	private int[][] weight;  // 表示图的二维矩阵，存储边的权重
	private boolean[][] shortest;  // 表示最短路径的矩阵，便于将路径高亮显示

	/**
	 * 从指定文件中读取内容，并根据内容构建有向图。
	 * @param filename 要读取的文件路径
	 * @return 如果构建图成功返回true，否则返回false
	 */
	private boolean createGraph(String filename) {
		File inputFile = new File(filename);
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(Files.newInputStream(inputFile.toPath())))) {
			initialize(); // 初始化图的数据结构
			Fileprocess(bReader); // 处理文件内容，构建有向图
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 初始化图的数据结构，包括单词列表、单词映射、边的权重矩阵和路径标记矩阵。
	 */
	private void initialize() {
		words = new ArrayList<>();
		wordMap = new HashMap<>();
		weight = new int[DEFAULT_ARRAY_SIZE][DEFAULT_ARRAY_SIZE];
		shortest = new boolean[DEFAULT_ARRAY_SIZE][DEFAULT_ARRAY_SIZE];
	}

	/**
	 * 处理文件内容，构建有向图。
	 * @param bReader 用于读取文件内容的BufferedReader
	 * @throws IOException 如果读取文件时发生错误则抛出 IOException
	 */
	private void Fileprocess(BufferedReader bReader) throws IOException {
		List<String> tempWordList = new ArrayList<>();  // 临时保存所有单词，从而分析边和权重
		while (bReader.ready()) {
			// 按行读入，去除非字母字符，将单词转换为小写
			String line = bReader.readLine().replaceAll("[^ a-zA-Z,.?!:;\"]+", "").toLowerCase();
			String[] lineArray = line.split("\\W+");  // 使用非单词字符进行分割
			for (String word : lineArray) {
				if (!word.isEmpty()) {  // 非空单词处理
					tempWordList.add(word);
					wordMap.putIfAbsent(word, words.size());  // 如果单词未出现过，则添加到wordMap和words中
					if (!words.contains(word)) {
						words.add(word);
					}
				}
			}
		}
		// 构建有向图，统计边的权重
		for (int i = 1; i < tempWordList.size(); i++) {
			String startWord = tempWordList.get(i - 1);
			String endWord = tempWordList.get(i);
			weight[wordMap.get(startWord)][wordMap.get(endWord)]++;  // 权重+1
		}
	}

	/**
	 * 查询两个单词之间的桥接词
	 * @param startWord 起始单词
	 * @param endWord 结束单词
	 * @return 返回桥接词列表，如果不存在或者输入单词不在词典中则返回null
	 */
	public List<String> queryBridgeWords(String startWord, String endWord) {
		// 清理输入单词，确保它们是有效的
		startWord = cleanWord(startWord);
		endWord = cleanWord(endWord);

		// 检查起始单词和结束单词是否在词典中
		if (!wordMap.containsKey(startWord) || !wordMap.containsKey(endWord)) {
			return null;
		}

		// 保存桥接词的列表
		List<String> bridgeWords = new ArrayList<>();
		// 获取起始单词和结束单词的索引
		int startIndex = wordMap.get(startWord);
		int endIndex = wordMap.get(endWord);

		// 遍历起始单词的所有邻接点，寻找桥接词
		for (int i = 0; i < weight[startIndex].length; i++) {
			if (weight[startIndex][i] > 0 && weight[i][endIndex] > 0) {
				bridgeWords.add(words.get(i));
				System.out.println("找到的桥接词: " + bridgeWords);
			}
		}
		if (bridgeWords.isEmpty()) {
			System.out.println("没有找到桥接词");
		}
		// 返回桥接词列表
		return bridgeWords;
	}

	/**
	 * 生成新文本，根据旧文本和桥接词
	 * @param oldText 旧文本
	 * @return 返回新文本
	 */
	public String generateNewText(String oldText) {
		// 创建一个新的StringBuilder来构建新文本
		StringBuilder newText = new StringBuilder();
		// 清理旧文本，确保它是有效的
		String cleanedText = cleanText(oldText);
		// 将清理后的文本按空格分割成单词数组
		String[] wordArray = cleanedText.toLowerCase().split(" ");

		// 创建一个随机数生成器
		Random random = new Random();
		// 初始化起始单词
		String start = wordArray[0];
		// 将起始单词添加到新文本中
		newText.append(start).append(" ");

		// 遍历每个单词，生成新文本
		for (int i = 1; i < wordArray.length; i++) {
			// 获取当前单词作为结束单词
			String end = wordArray[i];
			// 查询起始单词和结束单词之间的桥接词
			List<String> bridgeWords = queryBridgeWords(start, end);

			// 如果存在桥接词，则随机选择一个添加到新文本中
			if (!bridgeWords.isEmpty()) {
				int randomIndex = random.nextInt(bridgeWords.size());
				newText.append(bridgeWords.get(randomIndex)).append(" ");
			}

			// 将结束单词添加到新文本中，并将起始单词更新为当前结束单词
			newText.append(end).append(" ");
			start = end;
		}

		// 返回生成的新文本
		return newText.toString().trim();
	}

	/**
	 * 清理文本，去除非字母字符
	 * @param text 待清理的文本
	 * @return 返回清理后的文本
	 */
	private String cleanText(String text) {
		return text.replaceAll("[^ a-zA-Z,.?!:;\"]+", "").trim();
	}

	/**
	 * 清理单词，去除非字母字符
	 * @param word 待清理的单词
	 * @return 返回清理后的单词
	 */
	private String cleanWord(String word) {
		return word.replaceAll("[^ a-zA-Z,.?!:;\"]+", "");
	}


	/**
	 * Dijkstra方法查找两点间最短路径
	 * @param v0  起点
	 * @param v1 终点
	 */
	public void Dijkstra(int v0, int v1, String label) {
		int N = words.size();
		int[] path = new int[N];
		int[] dist = new int[N];
		boolean[] visited = new boolean[N];

		// 初始化路径长度为最大值
		for (int i = 0; i < N; i++) {
			dist[i] = DEFAULT_ARRAY_SIZE;
			visited[i] = false;
		}

		// 使用优先队列存储未访问的节点，按距离排序
		PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> dist[a] - dist[b]);

		dist[v0] = 0;
		pq.offer(v0);

		while (!pq.isEmpty()) {
			int u = pq.poll();
			if (visited[u]) continue;
			visited[u] = true;

			// 更新与节点u相邻节点的距离
			for (int v = 0; v < N; v++) {
				if (!visited[v] && weight[u][v] != 0 && dist[u] + weight[u][v] < dist[v]) {
					dist[v] = dist[u] + weight[u][v];
					path[v] = u;
					pq.offer(v);
				}
			}
		}

		// 输出最短路径
		if (dist[v1] != DEFAULT_ARRAY_SIZE) {
			System.out.println(words.get(v0) + " -> " + words.get(v1) + " 最短路径的长为" + dist[v1]);
			Stack<Integer> s = new Stack<Integer>();
			int u = v1;
			while (u != v0) {  // 将路径压栈
				s.push(u);
				int v = u;
				u = path[u];
				shortest[u][v] = true;
			}
			s.push(v0);
			while (!s.empty()) {
				System.out.print(words.get(s.pop()));
				if (!s.empty()) {
					System.out.print(" -> ");
				}
			}
			System.out.println("\n");
			String dotFormat = getAllPath();
			createDotGraph(dotFormat, "DGraph" + label);
		} else {
			System.out.println( " 不可达\n");
		}
	}

	/**
	 * 随机游走，输出由起始单词生成的文本，按空格停止遍历
	 * @param cin 用于读取输入的Scanner对象
	 */
	public void randomWalk(Scanner cin) {
		Random random = new Random();
		FileWriter writer = null;

		try {
			writer = new FileWriter("traversal_path.txt"); // 创建文件写入器

			// 从图中随机选择起始节点
			String currentWord = words.get(random.nextInt(words.size()));

			// 开始随机游走
			traverse(currentWord, new ArrayList<>(), writer, cin, random);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("\n游走完毕");
	}

	// 递归函数，实现随机游走
	private void traverse(String currentWord, List<String> visitedWords, FileWriter writer, Scanner cin, Random random) throws IOException {
		// 输出当前节点
		System.out.print(currentWord + " ");
		writer.write(currentWord + " ");

		// 将当前节点添加到已访问节点列表中
		visitedWords.add(currentWord);

		// 获取当前节点的所有邻接单词
		List<String> lin = getlin(currentWord);

		// 从邻接单词中随机选择一个进行继续游走
		List<String> neigh = new ArrayList<>(lin);
		neigh.removeAll(visitedWords);

		// 如果没有未访问的邻接单词，则结束游走
		if (neigh.isEmpty()) {
			return;
		}

		// 从未访问的邻接单词中随机选择一个进行继续游走
		String nextWord = neigh.get(random.nextInt(neigh.size()));

		// 等待用户输入
		String input = cin.nextLine();
		if (input.equals("s")) {
			return; // 用户停止游走
		}

		// 递归调用继续游走
		traverse(nextWord, visitedWords, writer, cin, random);
	}

	// 获取指定单词的所有邻接单词
	private List<String> getlin(String word) {
		int index = wordMap.get(word);
		List<String> lin = new ArrayList<>();
		for (int i = 0; i < weight[index].length; i++) {
			if (weight[index][i] != 0 && weight[index][i] != DEFAULT_ARRAY_SIZE) {
				lin.add(words.get(i));
			}
		}
		return lin;
	}
	/**
	 * 仿照API写的！！！
	 * @param dotFormat 图中所有路径的字符串
	 * @param fileName 生成的图文件
	 */
	public static void createDotGraph(String dotFormat,String fileName)
	{
		GraphViz g=new GraphViz();
		g.addln(g.start_graph());
		g.add(dotFormat);
		g.addln(g.end_graph());
		String type = "jpg";
		g.decreaseDpi();
		g.decreaseDpi();
		File out = new File(fileName+"."+ type);
		g.writeGraphToFile( g.getGraph( g.getDotSource(), type ), out );
	}


	/**
	 *
	 * @return 返回dot语法形式的字符串表示图的所有边
	 */
	public String getAllPath() {
		StringBuilder paths = new StringBuilder();
		for (int i = 0; i < words.size(); i++) {
			for (int j = 0; j < words.size(); j++) {
				if (weight[i][j] != 0 && weight[i][j] != DEFAULT_ARRAY_SIZE) {
					paths.append(words.get(i)).append("->").append(words.get(j));
					paths.append("[label=\"").append(weight[i][j]).append("\"");
					if (shortest[i][j]) {
						paths.append(", color=\"red\"");
					}
					paths.append("];");
				}
			}
		}
		return paths.toString();
	}


	/**
	 * 创建图文件
	 */
	void showGraph(){
		String dotFormat = getAllPath();
		createDotGraph(dotFormat, "DGraph");
		new show("DGraph.jpg");
	}

	/**
	 * 计算两个单词之间的最短路径，并展示计算结果
	 * @param word1 第一个单词
	 * @param word2 第二个单词
	 */
	public void calcShortestPath(String word1, String word2) {
		// 清理输入的单词，确保它们是有效的
		word1 = cleanWord(word1);
		word2 = cleanWord(word2);

		// 检查单词是否存在于图中
		if (!words.contains(word1) || !words.contains(word2)) {
			System.out.println("No " + word1 + " or " + word2 + " in the graph!");
			return;
		}

		// 计算两个单词之间的最短路径，并展示结果
		Dijkstra(wordMap.get(word1), wordMap.get(word2), "Calc");
		new show("DGraphCalc.jpg");
	}


	/**
	 * 某单词到任意单词间的最短路径，并展示计算结果
	 * @param word 起始单词
	 */
	void calcShortestPath(String word) {
		// 清理输入的单词，确保它是有效的
		word = cleanWord(word);

		// 检查单词是否存在于图中
		if (!words.contains(word)) {
			System.out.println("No " + word + " in the graph!");
			return;
		}

		// 生成随机目标单词索引
		Random random = new Random();
		int rand = random.nextInt(words.size());

		// 计算起始单词到每个单词之间的最短路径，并展示结果
		for (int i = 0; i < words.size(); i++) {
			Dijkstra(wordMap.get(word), i, word + "To" + words.get(i));
			if (i == rand) {
				new show("DGraph" + word + "To" + words.get(i) + ".jpg");
			}
		}
	}
	}



