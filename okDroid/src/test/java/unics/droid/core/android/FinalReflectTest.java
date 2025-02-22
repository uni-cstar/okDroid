package unics.droid.core.android;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Create by luochao
 * on 2023/10/17
 */
public class FinalReflectTest {
    /**
     * java是通过modifiers来修改修饰符
     */
    @Test
    public void javaReflectFinalField(){
        try{
            Person person = new Person();
            Field ageField =  Person.class.getDeclaredField("age");
            ageField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(ageField, ageField.getModifiers() & ~Modifier.FINAL);

            ageField.set(person,10);
            System.out.println("the age is " + person.age + " ---" + person.getDisplayAge());
//            Field modifiersField =
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field,field.getModifiers()&~Modifier.FINAL);
//            field.set(null, new char[]{'1', '2', '3'});
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 4.4是通过slot来改修饰符
     */
    @Test
    public void android4_4_ReflectFinalField(){
        try{
            Person person = new Person();
            Field ageField =  Person.class.getDeclaredField("age");
            ageField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("slot");
            modifiersField.setAccessible(true);
            modifiersField.setInt(ageField,  modifiersField.getInt(ageField) & ~Modifier.FINAL);
            ageField.set(person, 10);
            System.out.println("the age is " + person.age + " ---" + person.getDisplayAge());
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 6.0以上是通过accessFlags来改修饰符
     */
    @Test
    public void android6ReflectFinalField(){
        try{
            Person person = new Person();
            Field ageField =  Person.class.getDeclaredField("age");
            ageField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("accessFlags");
            modifiersField.setAccessible(true);
            modifiersField.setInt(ageField, ageField.getModifiers() & ~Modifier.FINAL);

            ageField.set(person,10);
            System.out.println("the age is " + person.age + " ---" + person.getDisplayAge());
        }catch (Throwable e){
            e.printStackTrace();
        }
    }


}
