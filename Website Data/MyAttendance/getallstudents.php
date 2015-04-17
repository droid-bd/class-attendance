<?php

    $response=array();
    //$categoryParam=$_REQUEST['Student_Department'];

    require_once __DIR__.'/db_connect.php';
    $db=new DB_CONNECT();

    //$sql="SELECT * from books where Student_Department like '%".$categoryParam."%'";
    $sql="SELECT * from student_info";
    $result=mysql_query($sql);

    if(!empty($result))
    {
        if(mysql_num_rows($result)>0)
        {
            $response['students']=array();
           while ($row = mysql_fetch_array($result)) {
               $students=array();
                $students['Student_Id']=$row['Student_Id'];
                $students['Student_Name']=$row['Student_Name'];
                $students['Student_Mobile']=$row['Student_Mobile'];
                $students['Student_Email']=$row['Student_Email'];
                $students['Student_B_Group']=$row['Student_B_Group'];
                $students['Student_Address']=$row['Student_Address'];
                $students['Student_Department']=$row['Student_Department'];
                $students['Student_Position']=$row['Student_Position'];
                array_push($response['students'], $students);
            }
            $response['success']=1;
            echo json_encode($response);
        }
        else {
            $response['success']=0;
            $response['message']='No students found';

            echo json_encode($response);
        }
    }
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
?>

