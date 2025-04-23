
package com.mycompany.jsondiff;

import java.util.List;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import static org.neo4j.driver.Values.parameters;



public class Conexao implements AutoCloseable {
    
    Driver driver = GraphDatabase.driver("bolt://localhost:7687/", AuthTokens.basic("neo4j", "jsondiff"));
    
     @Override
    public void close() throws RuntimeException {
        driver.close();
    }

  public void criaNodeNeo4j(List<Diff> lista, Pessoa pessoa, String fileName1, String fileName2){
      
    try(Session session = driver.session()){
       
                //adicionando nÃ³ pessoa
                session.run("MERGE (AA:Agente{name:$name})",
                        parameters("name", pessoa.getName()));
                //criando os nÃ³s 
                for (Diff diff : lista) {            
                 
            
                session.run("MERGE (e:Entidade {op:$op, name: $name, versao:$versao})",
                          parameters("op", diff.getOp(),"name", diff.getName(), "versao", diff.getVersao()));
                session.run("MERGE (a:Atividade {op:$op, ultimoPath:$ultimoPath, name:$name, versao:$versao})",
                        parameters("op", diff.getOp()+diff.getUltimoPath(), "ultimoPath", diff.getUltimoPath(), "name", diff.getName(), "versao", diff.getVersao()));
                session.run("CREATE(p:Propriedade {op:$op, name:$name, ultimoPath:$ultimoPath, versao:$versao, value:$value })",
                          parameters("op", diff.getOp()+diff.getUltimoPath(), "name", diff.getName(), "ultimoPath", diff.getUltimoPath(), "versao", diff.getVersao(), "value", diff.getValue()));

                    System.out.println("id: " + diff.getId());                   
                    System.out.println("op: " + diff.getOp());
                    System.out.println("path: " + diff.getPath());
                    System.out.println("value: " + diff.getValue());
                    System.out.println("name: " + diff.getName());
                    System.out.println("type: " + diff.getType());
                    System.out.println("ultimo path: " + diff.getUltimoPath());
                    System.out.println("versão: " + diff.getVersao());
                    System.out.println("----------");
                   
                 
                 //RELACIONANDO AGENTE E ATIVIDADE 
                    session.run("MATCH(AA:Agente), (a:Atividade)" +
                       "WHERE AA.name = '"+pessoa.getName()+"' AND a.op = '"+diff.getOp()+diff.getUltimoPath()+"'"+
                       "MERGE (AA)<-[:wasAssociatedWith]-(a)");     //verificar a direção dos relacionamentos no PROV                          

                    //RELACIONANDO ATIVIDADE E PROPRIEDADE 
                    session.run("MATCH(a:Atividade), (p:Propriedade)" + 
                          "WHERE a.op = '"+diff.getOp()+diff.getUltimoPath()+"' AND p.op = '"+diff.getOp()+diff.getUltimoPath()+"' AND a.name = '"+diff.getName()+"' AND p.name = '"+diff.getName()+"' AND a.versao = "+diff.getVersao()+" AND p.versao = "+diff.getVersao()+" " +
                           "MERGE (a)<-[:wasGeneratedBy]-(p)"); //dúvida
                    //relacionamento que já existe, não faz nada. só cria quando qndo for diferente. 
                    
                 //RELACIONANDO PROPRIEDADE E ENTIDADE 
                    session.run("MATCH(p:Propriedade), (e:Entidade)" + //tirei o ultimo path, pois as entidades estavam duplicando
                        "WHERE p.name = '"+diff.getName()+"' AND e.name = '"+diff.getName()+"' AND p.op = '"+diff.getOp()+diff.getUltimoPath()+"' AND e.op = '"+diff.getOp()+"'AND p.versao = "+diff.getVersao()+" AND e.versao = "+diff.getVersao()+" "+ 
                        "MERGE (p)<-[:hadMember]-(e)"); 
            
                //RELACIONANDO ENTIDADES  
                    session.run("MATCH (e1:Entidade), (e2:Entidade)" +
                      "WHERE e1.name = e2.name AND e2.versao = e1.versao - 1 "+
                      "MERGE (e1)<-[:wasDerivedFrom]-(e2)"); 
                    

           }
    }finally 
        
            {
            driver.close();
            }
    
    System.out.print("Foram encontrados um total de "+lista.size()+" diferenças entre os arquivos "+fileName1+" e "+fileName2+".");

  }   
  
}