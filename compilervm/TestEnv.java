import common.Coord;
import common.Environment;
import common.Maybe;

public class TestEnv {
    public static void main( String[] args ) {
        var myEnv = new Environment();

        myEnv.enter();

        myEnv.set( "Gandalf", 0 );
        myEnv.set( "Frodo", 1 );
        myEnv.set( "Sam", 2 );

        var result = myEnv.lookUp( "Frodo" );
        assert( result.hasElement() == true );
        var coord = result.getElement();
        assert( coord.first() == 0 && coord.second() == 1 );

        myEnv.enter();

        myEnv.set( "Sauron", 0 );
        myEnv.set( "Saruman", 1 );
        myEnv.set( "Gollum", 2 );

        result = myEnv.lookUp( "Gandalf" );
        assert( result.hasElement() == true );
        coord = result.getElement();
        assert( coord.first() == 1 && coord.second() == 0 );

        result = myEnv.lookUp( "Gollum" );
        assert( result.hasElement() == true );
        coord = result.getElement();
        assert( coord.first() == 0 && coord.second() == 2 );

        result = myEnv.lookUp( "Manwa" );
        assert( result.hasElement() == false );

        myEnv.leave();

        result = myEnv.lookUp( "Sam" );
        assert( result.hasElement() == true );
        coord = result.getElement();
        assert( coord.first() == 0 && coord.second() == 2 );

        myEnv.leave();
    }
}
