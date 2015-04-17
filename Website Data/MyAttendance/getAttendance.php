<?php
    $t_id = $_REQUEST['t_id'];
    $password = $_REQUEST['password'];
    
    //$t_id = "123";
    //$password = "456";
    
    require_once __DIR__.'/db_connect.php';
    $db = new DB_CONNECT();
    
    $sql = "SELECT * FROM attendance WHERE Teacher_Id='". $t_id ."' AND password='". $password ."'";
    $result = mysql_query($sql);
    
    $response = array();
    
    if(!empty($result))
    {
        if(mysql_num_rows($result)>0)
        {
            $response['teacher'] = array();
            while($row = mysql_fetch_array($result))
            {
                $tcr = array();
                $tcr['t_id'] = $row['Teacher_Id'];
                $tcr['s_id'] = $row['Student_Id'];
                $tcr['g_id'] = $row['GROUPD'];
                $tcr['c_no'] = $row['Course_No'];
                $tcr['d_id'] = $row['Department_Id'];
                $tcr['date'] = $row['Date'];
                $tcr['password'] = $row['password'];
                
                array_push($response['teacher'], $tcr);                
            }
            $response['success'] = 1;
            echo json_encode($response);
        }                
    }
    else 
    {
        $response['success'] = 0;
        echo json_encode($response);
    }

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
?>
