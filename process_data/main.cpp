#include <iostream>
#include "rapidxml_utils.hpp"

using namespace rapidxml;
using std::cout;

void explore_chidren(xml_node<> *parent){

  if(!parent) return;

  for (xml_node<> *i = parent->first_node();
       i; i = i->next_sibling() )
  {

    if( (!strcmp(i->name(), "content") ||
     !strcmp(i->name(), "subject") ) && strlen( i->value() )!=0 ) {
    cout << " <TEXT>\n" << i->value() << "</TEXT>\n";
  }
  explore_chidren( i );

  }



}

int main(){
    file<> xmlFile("clean_class_content.xml"); // Default template is char
    xml_document<> doc;
    doc.parse<0>(xmlFile.data());
    explore_chidren(&doc);
  return 0;
}
