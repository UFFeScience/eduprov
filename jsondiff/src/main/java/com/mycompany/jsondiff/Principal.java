
package com.mycompany.jsondiff;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import java.nio.file.Files;  
import java.nio.file.Paths;  
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Principal {

    public static void main(String[] args) throws Exception {
                 
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("Digite o nome do autor dos JSONS: "); 
            Pessoa pessoa = new Pessoa();
            String nome = scanner.nextLine();
            pessoa.setName(nome);

            System.out.println("Digite a localização da pasta que contém os arquivos para comparação: "); 
            String location = scanner.nextLine();

            int contador = 1;
            while(contador > 0){    
                
            String file1 = "";  
            String file2 = "";
            
            System.out.println("\nDigite o nome do primeiro arquivo: ");
            String fileName1 = scanner.nextLine() + ".json";
            
            String fileName2 = "";
            
            if(contador > 1){
                file1 = convertFileIntoString(location+"\\"+fileName1);
                
                System.out.println("\nDigite o nome do segundo arquivo: ");
                fileName2 = scanner.nextLine() + ".json";

                //Converte o arquivo json em String e salva na variável
                file2 = convertFileIntoString(location+"\\"+fileName2); 
            } 
            else if(contador == 1){
                file1 = convertFileIntoString(location+"\\"+fileName1);
                file2 = file1;
                file1 = contarComponentes(file1);         
            }      
            
            ObjectMapper mapper = new ObjectMapper();
            
            //Lê a string colocando ela no formato JsonNode
            JsonNode node1 = mapper.readTree(file1);
            JsonNode node2 = mapper.readTree(file2);
            
            //Comparação usando o import zjsonpatch.JsonDiff retornando TODAS as diferenças
            String diff = JsonDiff.asJson(node1, node2).toString();  
              
            try {
                
            //List<String> pathsList = new ArrayList<>();
            JSONArray jsonArray2 = new JSONArray(diff);
            List<Diff> diferencaList = new ArrayList<>();            
            int numerador = 0;

                for (Object obj : jsonArray2) {
                    JSONObject jsonObject = (JSONObject) obj;
                    Diff diferenca = new Diff();
                    
                    diferenca.setId(numerador);
                    diferenca.setOp(jsonObject.get("op").toString());  
                    diferenca.setPath(jsonObject.get("path").toString()); 
                    diferenca.setVersao(contador);
                    
                    if(!diferenca.getOp().equals("remove") && !diferenca.getOp().equals("copy") && !diferenca.getOp().equals("move")){  
                        diferenca.setValue(jsonObject.get("value").toString());  
                    }
                    //pathsList.add(path);
                    String[] pathDividido = diferenca.getPath().split("/");                      
                    
                    JSONObject jsonComparador = new JSONObject(file2);  
                    
                    Map<String, Integer> contagem = contarOcorrencias(pathDividido);
                    
                    //Pedido da Naira
                    String ultimoNaoNumerico = null;
                    
                    for (String componente : pathDividido) {
                        // Verifica se o componente não é um número
                        if (!componente.matches("^\\d+$")) {
                            ultimoNaoNumerico = componente;
                        }
                    }

                    String alvo = "$Components";
                    int ocorrencias = contagem.getOrDefault(alvo, 0);                              

                    for(int i=1; i < pathDividido.length ; i++){
                        
                    try {
                         
                        if(pathDividido[i].equals("Properties") && pathDividido.length == 3){
                            JSONObject properties = jsonComparador.getJSONObject("Properties");
                            diferenca.setName(properties.getString("$Name"));
                            diferenca.setType(properties.getString("$Type"));
                            diferenca.setUltimoPath(ultimoNaoNumerico);
                        }
                        else if(pathDividido[i].equals("$Components") && ocorrencias == 1){
                                                    
                            JSONObject properties = jsonComparador.getJSONObject(pathDividido[i - 1]);
                                  
                            JSONArray componentsArray = properties.getJSONArray(pathDividido[i]);
                            JSONObject secondComponent = componentsArray.getJSONObject(Integer.parseInt(pathDividido[i + 1]));
                            diferenca.setName(secondComponent.getString("$Name"));
                            diferenca.setType(secondComponent.getString("$Type"));
                            diferenca.setUltimoPath(ultimoNaoNumerico);
                        }
                        else if (pathDividido[i].equals("$Components") && ocorrencias > 1 && i == 4) {
                            
                            JSONObject properties = jsonComparador.getJSONObject(pathDividido[1]);
                            
                            
                            JSONArray componentsArray = properties.getJSONArray(pathDividido[2]);
                            JSONObject secondComponent = componentsArray.getJSONObject(Integer.parseInt(pathDividido[3]));
                            JSONArray componentsArray2 = secondComponent.getJSONArray(pathDividido[4]);
                            JSONObject secondComponent2 = componentsArray2.getJSONObject(Integer.parseInt(pathDividido[5]));
                            diferenca.setName(secondComponent2.getString("$Name"));
                            diferenca.setType(secondComponent2.getString("$Type"));
                            diferenca.setUltimoPath(ultimoNaoNumerico);
                            
                        }
                        }
                    catch (ArrayIndexOutOfBoundsException | JSONException e) {
                        // Tratamento da exceção ArrayIndexOutOfBoundsException
                            diferenca.setName("Components");
                            diferenca.setType("Components");
                            diferenca.setUltimoPath(ultimoNaoNumerico);
              
                        }
                    }                    
                  
                diferencaList.add(diferenca);
                numerador++;
                    
                } 
                
               Conexao conexao = new Conexao();
               conexao.criaNodeNeo4j(diferencaList, pessoa, fileName1, fileName2 );   
                      
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            String resposta = "";
            
            if(contador > 1){
                System.out.println("\nDeseja fazer mais comparações? Escreva S ou N");
                resposta = scanner.nextLine();
            }
            else if(contador == 1){
                resposta = "S";
            }
            
                switch (resposta) {
                    case "N":
                    case "n":
                        contador = -1;
                        break;
                    case "S":
                    case "s":
                        contador++;
                        break;
                    default:
                        System.out.println("Comando inválido!\nAbortando execução...\nPor favor execute a classe novamente.");
                        contador = -1;
                        break;
                }

            }
            }
      
        //Método para fazer a leitura do arquivo e transforma-lo em string
        public static String convertFileIntoString(String file)throws Exception  
        {  

            String result;  
            result = new String(Files.readAllBytes(Paths.get(file)));  

            return result;  
        } 
    
        private static void escreverJsonNoArquivo(String jsonString, String caminhoArquivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            // Escrever a string JSON no arquivo
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
        
        public static boolean isNumeric(String str) {
        try {
            int e = Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
        }
        
        public static Map<String, Integer> contarOcorrencias(String[] lista) {
        Map<String, Integer> contagem = new HashMap<>();
        
        for (String str : lista) {
            contagem.put(str, contagem.getOrDefault(str, 0) + 1);
        }
        
        return contagem;
        }
        
        public static String contarComponentes(String primeiroJson){
        
            
            
            
            JSONObject json = new JSONObject(primeiroJson);
            JSONArray componentsArray = json.getJSONObject("Properties").getJSONArray("$Components");
            int quantidade = componentsArray.length();
            System.out.println(quantidade);
           
            String file1 = "{\"authURL\":[\"creator.kodular.io\"],\"YaVersion\":\"242\",\"Source\":\"Form\",\"Properties\":{\"$Components\":[x]}}";
            String file2 = "{\"$Components\":[]}";       
            
            StringBuilder resultado = new StringBuilder();
            for (int i = 0; i < quantidade; i++) {
                resultado.append(file2);
                if (i < quantidade - 1) {
                    resultado.append(",");
                }
            }
            
            String resultadoString = file1.replace("x", resultado.toString());     
            System.out.println(resultadoString);
            
            return resultadoString;
        }
       
        
} 