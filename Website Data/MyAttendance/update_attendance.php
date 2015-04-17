<?php
    $t_id = $_REQUEST['t_id'];
    $s_id = $_REQUEST['s_id'];
    $c_id = $_REQUEST['c_id'];
    $grp = $_REQUEST['grp'];
    $d_id = $_REQUEST['d_id'];
    $date = $_REQUEST['date'];
    $password = $_REQUEST['password'];

    require_once __DIR__.'/db_connect.php';
    $db = new DB_CONNECT();
    
    $sql = "SELECT * FROM attendance WHERE "
            . "Teacher_Id='". $t_id 
            . "' AND Student_Id='" . $s_id 
            . "' AND GROUPD='" . $grp 
            . "' AND Course_No='" . $c_id 
            . "' AND Department_Id='" . $d_id 
            . "' AND Date='" . $date
            . "' AND password='" . $password ."'";
    
    $result = mysql_query($sql);
    
    $response = array();
    
    if(mysql_num_rows($result)>0)
    {
        $response['success'] = 1;
        echo json_encode($response);        
    }
    else 
    {
        $sql = "insert into attendance values('". $t_id .
                "', '". $s_id .
                "', '". $grp .
                "', '". $c_id .
                "', '". $d_id .
                "', '". $date .
                "', '". $password ."')";
        $inserted = mysql_query($sql);

        $response = array();

        if($inserted)
        {
            $response['success'] = 1;
            echo json_encode($response);
        }
        else 
        {
            $response['success'] = 0;
            echo json_encode($response);
        }
    }
    

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
?>
